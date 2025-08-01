package com.masterello.auth.config;

import com.masterello.auth.customgrants.googlegrant.GoogleAuthCodeAuthenticationConverter;
import com.masterello.auth.customgrants.googlegrant.GoogleAuthCodeAuthenticationProvider;
import com.masterello.auth.customgrants.passwordgrant.CustomPasswordAuthenticationConverter;
import com.masterello.auth.customgrants.passwordgrant.CustomPasswordAuthenticationProvider;
import com.masterello.auth.refresh.CustomOauth2RefreshTokenAuthenticationProvider;
import com.masterello.auth.repository.CustomStatelessAuthorizationRequestRepository;
import com.masterello.auth.requestresolver.GoogleAuthenticationRequestResolver;
import com.masterello.auth.responsehandlers.GoogleFailureAuthHandler;
import com.masterello.auth.responsehandlers.GoogleSuccessAuthHandler;
import com.masterello.auth.responsehandlers.Oauth2LogoutSuccessAuthHandler;
import com.masterello.auth.responsehandlers.Oauth2SuccessAuthHandler;
import com.masterello.auth.responsehandlers.TokenAuthenticationFailureHandler;
import com.masterello.auth.revocation.LogoutRevocationAuthenticationProvider;
import com.masterello.auth.revocation.LogoutRevocationEndpointAuthenticationConverter;
import com.masterello.commons.security.config.SuperAdminProperties;
import com.masterello.commons.security.filter.SuperAdminFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final CustomStatelessAuthorizationRequestRepository authorizationRequestRepository;
    private final CustomPasswordAuthenticationProvider customPassordAuthenticationProvider;
    private final GoogleAuthCodeAuthenticationProvider googleAuthCodeAuthenticationProvider;
    private final LogoutRevocationAuthenticationProvider logoutRevocationAuthenticationProvider;
    private final CustomOauth2RefreshTokenAuthenticationProvider refreshTokenAuthenticationProvider;
    private final TokenAuthenticationFailureHandler tokenAuthenticationFailureHandler;
    private final GoogleSuccessAuthHandler googleSuccessAuthHandler;
    private final GoogleFailureAuthHandler googleErrorHandler;
    private final SuperAdminProperties superAdminProperties;

    /**
     * Security config for token management endpoints:
     * * get access token by supported grant types
     * * refresh tokens
     * * etc
     */
    @Bean
    @Order(1)
    public SecurityFilterChain tokenManagementAuthFilter(HttpSecurity http) throws Exception {

        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();

        authorizationServerConfigurer
                .tokenEndpoint(tokenEndpoint ->
                        tokenEndpoint
                                .accessTokenResponseHandler(new Oauth2SuccessAuthHandler())
                                .accessTokenRequestConverter(new CustomPasswordAuthenticationConverter())
                                .accessTokenRequestConverter(new GoogleAuthCodeAuthenticationConverter())
                                .authenticationProvider(googleAuthCodeAuthenticationProvider)
                                .authenticationProvider(customPassordAuthenticationProvider)
                                .authenticationProvider(refreshTokenAuthenticationProvider)
                                .errorResponseHandler(tokenAuthenticationFailureHandler))
                .tokenRevocationEndpoint(revocationEndpoint ->
                        revocationEndpoint
                                .revocationResponseHandler(new Oauth2LogoutSuccessAuthHandler())
                                .revocationRequestConverter(new LogoutRevocationEndpointAuthenticationConverter())
                                .authenticationProvider(logoutRevocationAuthenticationProvider));

        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

        http
                .securityMatcher(endpointsMatcher)
                .authorizeHttpRequests(authorize ->
                        authorize
                                .anyRequest().authenticated()
                )
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .apply(authorizationServerConfigurer);

        return http.build();
    }


    /**
     * Security config for Google auth endpoints
     */
    @Bean
    @Order(2)
    public SecurityFilterChain googleAuthFilter(HttpSecurity http, ClientRegistrationRepository clientRegistrationRepository) throws Exception {

        http
                .securityMatcher("/oauth2/authorization/google", "/login/oauth2/code/google**", "/login/google**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login/google**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2Login -> oauth2Login
                        .loginPage("/oauth2/authorization/google")
                        .successHandler(googleSuccessAuthHandler)
                        .failureHandler(googleErrorHandler)
                        .authorizationEndpoint(subconfig ->
                                subconfig
                                        .authorizationRequestResolver(new GoogleAuthenticationRequestResolver(clientRegistrationRepository, "/oauth2/authorization"))
                                        .authorizationRequestRepository(authorizationRequestRepository)
                        )
                )
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    /**
     * Security config for client registration
     */
    @Bean
    @Order(3)
    public SecurityFilterChain authApiFilter(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/auth/client")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated())
                .addFilterBefore(new SuperAdminFilter(superAdminProperties), AnonymousAuthenticationFilter.class)
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    /**
     * Actuator config
     */
    @Bean
    @Order(4)
    public SecurityFilterChain actuator(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/actuator/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll())
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
