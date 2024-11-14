package com.masterello.worker.config;

import com.masterello.auth.service.AuthService;
import com.masterello.commons.security.filter.AuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class WorkerApiSecurityConfig {

    private final AuthService authService;

    @Bean
    public SecurityFilterChain apiWorkerFilter(HttpSecurity http) throws Exception {
        RequestMatcher publicEndpoints = new OrRequestMatcher(
                new AntPathRequestMatcher("/api/worker/search"),
                new AntPathRequestMatcher("/api/worker/*/full-info")
        );
        AuthFilter authFilter = new AuthFilter(
                new NegatedRequestMatcher(publicEndpoints), authService);

        http
                .securityMatcher("/api/worker/**")
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicEndpoints).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(authFilter, AnonymousAuthenticationFilter.class)
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
