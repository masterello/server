package com.masterello.worker.config;

import com.masterello.auth.service.AuthService;
import com.masterello.commons.security.filter.AuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
@EnableConfigurationProperties(WorkerConfigProperties.class)
public class WorkerApiSecurityConfig {

    private final AuthService authService;

    @Bean
    public SecurityFilterChain apiWorkerFilter(HttpSecurity http) throws Exception {
        var matcherBuilder = PathPatternRequestMatcher.withDefaults();
        RequestMatcher publicEndpoints = new OrRequestMatcher(
                matcherBuilder.matcher("/api/worker/search"),
                matcherBuilder.matcher("/api/worker/*/full-info"),
                matcherBuilder.matcher("/api/worker/supported-languages")
        );
        AuthFilter authFilter = new AuthFilter(
                new NegatedRequestMatcher(publicEndpoints), authService);

        http
                .securityMatcher(matcherBuilder.matcher("/api/worker/**"))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(publicEndpoints).permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(authFilter, AnonymousAuthenticationFilter.class)
                .sessionManagement(c -> c.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
