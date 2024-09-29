package com.masterello.user.config;

import com.masterello.filter.AuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    @Bean
    public SecurityFilterChain apiAuthFilter(HttpSecurity http) throws Exception {
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
}
