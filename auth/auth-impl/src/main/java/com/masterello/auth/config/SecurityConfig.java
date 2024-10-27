package com.masterello.auth.config;

import com.masterello.auth.customgrants.googlegrant.GoogleOidAuthenticationConverter;
import com.masterello.auth.customgrants.googlegrant.GoogleOidAuthenticationProvider;
import com.masterello.auth.customgrants.passwordgrant.CustomPasswordAuthenticationConverter;
import com.masterello.auth.customgrants.passwordgrant.CustomPasswordAuthenticationProvider;
import com.masterello.auth.repository.CustomStatelessAuthorizationRequestRepository;
import com.masterello.auth.responsehandlers.GoogleSuccessAuthHandler;
import com.masterello.auth.responsehandlers.Oauth2IntrospectSuccessAuthHandler;
import com.masterello.auth.responsehandlers.Oauth2LogoutSuccessAuthHandler;
import com.masterello.auth.responsehandlers.TokenAuthenticationFailureHandler;
import com.masterello.auth.revocation.LogoutRevocationAuthenticationProvider;
import com.masterello.auth.revocation.LogoutRevocationEndpointAuthenticationConverter;
import com.masterello.commons.security.filter.SuperAdminFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
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
    private final GoogleOidAuthenticationProvider googleOidAuthenticationProvider;
    private final LogoutRevocationAuthenticationProvider logoutRevocationAuthenticationProvider;
    private final Oauth2IntrospectSuccessAuthHandler introspectSuccessAuthHandler;
    private final TokenAuthenticationFailureHandler tokenAuthenticationFailureHandler;
    private final GoogleSuccessAuthHandler googleSuccessAuthHandler;
    private final SuperAdminFilter superAdminFilter;

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
                                .accessTokenRequestConverter(new CustomPasswordAuthenticationConverter())
                                .accessTokenRequestConverter(new GoogleOidAuthenticationConverter())
                                .authenticationProvider(googleOidAuthenticationProvider)
                                .authenticationProvider(customPassordAuthenticationProvider)
                                .errorResponseHandler(tokenAuthenticationFailureHandler))
                .tokenRevocationEndpoint(revocationEndpoint ->
                        revocationEndpoint
                                .revocationResponseHandler(new Oauth2LogoutSuccessAuthHandler())
                                .revocationRequestConverter(new LogoutRevocationEndpointAuthenticationConverter())
                                .authenticationProvider(logoutRevocationAuthenticationProvider))
                .tokenIntrospectionEndpoint(introspectEndpoint ->
                        introspectEndpoint.introspectionResponseHandler(introspectSuccessAuthHandler)
                );

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
    public SecurityFilterChain googleAuthFilter(HttpSecurity http) throws Exception {

        http
                .securityMatcher("/oauth2/authorization/google", "/login", "/login/oauth2/code/google**", "/login/google**") // TODO remove /login from here, it shouldn't be processed by this ms
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/login/google**").permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2Login -> oauth2Login
                        .successHandler(googleSuccessAuthHandler)
                        .authorizationEndpoint(subconfig -> {
                            subconfig.authorizationRequestRepository(this.authorizationRequestRepository);
                        })
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
                .addFilterBefore(superAdminFilter, AnonymousAuthenticationFilter.class)
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
