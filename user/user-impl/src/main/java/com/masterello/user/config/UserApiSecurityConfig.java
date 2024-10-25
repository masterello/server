package com.masterello.user.config;

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

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class UserApiSecurityConfig {

    private final AuthFilter authFilter;
    private final SuperAdminFilter superAdminFilter;

    @Bean
    @Order(1)  // Higher priority to match this first
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/user/admin", "/api/user/admin/**")  // Apply only to /api/user/admin
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated())  // Require authentication for /api/user/admin
                .addFilterBefore(superAdminFilter, AnonymousAuthenticationFilter.class)
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS));  // Stateless session management

        return http.build();
    }
    @Bean
    @Order(2)
    public SecurityFilterChain apiUserFilter(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/user/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/user/signup", "/api/user/confirmationLink/**", "/api/user/supported-languages").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(authFilter, AnonymousAuthenticationFilter.class)
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
    @Bean
    @Order(3)
    public SecurityFilterChain apiSupportFilter(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/api/support/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/support/contact").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(authFilter, AnonymousAuthenticationFilter.class)
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
