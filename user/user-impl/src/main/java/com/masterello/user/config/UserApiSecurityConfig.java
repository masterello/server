package com.masterello.user.config;

import com.masterello.auth.service.AuthService;
import com.masterello.commons.security.config.SuperAdminProperties;
import com.masterello.commons.security.filter.AuthFilter;
import com.masterello.commons.security.filter.SuperAdminFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class UserApiSecurityConfig {

    private final AuthService authService;
    private final SuperAdminProperties superAdminProperties;

    @Bean
    @Order(1)  // Higher priority to match this first
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        PathPatternRequestMatcher.Builder matcherBuilder = PathPatternRequestMatcher.withDefaults();
        http
                .securityMatcher("/api/user/admin", "/api/user/admin/**")  // Apply only to /api/user/admin
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated())  // Require authentication for /api/user/admin
                .addFilterBefore(new SuperAdminFilter(superAdminProperties), AnonymousAuthenticationFilter.class)
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS));  // Stateless session management

        return http.build();
    }
    @Bean
    @Order(2)
    public SecurityFilterChain apiUserFilter(HttpSecurity http) throws Exception {
        PathPatternRequestMatcher.Builder matcherBuilder = PathPatternRequestMatcher.withDefaults();
        RequestMatcher publicEndpoints = new OrRequestMatcher(
                matcherBuilder.matcher("/api/user/signup"),
                matcherBuilder.matcher("/api/user/confirmationLink/**"),
                matcherBuilder.matcher("/api/user/resetPassword/**")
        );
        AuthFilter authFilter = new AuthFilter(
                new NegatedRequestMatcher(publicEndpoints), authService);

        http
                .securityMatcher(matcherBuilder.matcher("/api/user/**"))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicEndpoints).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(authFilter, AnonymousAuthenticationFilter.class)
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
    @Bean
    @Order(3)
    public SecurityFilterChain apiSupportFilter(HttpSecurity http) throws Exception {
        PathPatternRequestMatcher.Builder matcherBuilder = PathPatternRequestMatcher.withDefaults();
        RequestMatcher publicEndpoints = matcherBuilder.matcher("/api/support/contact");

        AuthFilter authFilter = new AuthFilter(
                new NegatedRequestMatcher(publicEndpoints), authService);

        http
                .securityMatcher(matcherBuilder.matcher("/api/support/**"))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicEndpoints).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(authFilter, AnonymousAuthenticationFilter.class)
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
