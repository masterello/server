package com.masterello.task.configuration

import com.masterello.auth.service.AuthService
import com.masterello.commons.security.filter.AuthFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.OrRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

@Configuration
@EnableWebSecurity
open class TaskSecurityConfig(private val authService: AuthService) {

    @Bean
    @Throws(Exception::class)
    open fun apiTaskAuthFilter(http: HttpSecurity): SecurityFilterChain {

        val protectedEndpoints: RequestMatcher = OrRequestMatcher(
                AntPathRequestMatcher("/api/tasks/{taskUuid}", "GET"),
                AntPathRequestMatcher("/api/tasks/user/{userUuid}/search", "POST"),
                AntPathRequestMatcher("/api/tasks/worker/{workerUuid}/search", "POST"),
                AntPathRequestMatcher("/api/tasks", "POST"),
                AntPathRequestMatcher("/api/tasks/search", "POST"),
                AntPathRequestMatcher("/api/tasks/worker/search", "POST"),
                AntPathRequestMatcher("/api/tasks/{taskUuid}/update", "POST"),
                AntPathRequestMatcher("/api/tasks/{taskUuid}/assign", "POST"),
                AntPathRequestMatcher("/api/tasks/{taskUuid}/unassign", "POST"),
                AntPathRequestMatcher("/api/tasks/{taskUuid}/reassign", "POST"),
                AntPathRequestMatcher("/api/tasks/{taskUuid}/complete", "POST"),
                AntPathRequestMatcher("/api/tasks/{taskUuid}/cancel", "POST"),
                AntPathRequestMatcher("/api/tasks/worker-review", "POST"),
                AntPathRequestMatcher("/api/tasks/user-review", "POST"),
                AntPathRequestMatcher("/api/tasks/{taskUuid}/worker/{workerUuid}/confirm", "POST"),
        )

        val authFilter = AuthFilter(protectedEndpoints, authService)

        http
                .securityMatcher(AntPathRequestMatcher("/api/tasks/**"))
                .csrf { it.disable() }
                .authorizeHttpRequests { auth ->
                    auth
                            .requestMatchers(protectedEndpoints).authenticated()  // Require authentication for specific endpoints
                            .anyRequest().permitAll()
                }
                .addFilterBefore(authFilter, AnonymousAuthenticationFilter::class.java)
                .sessionManagement { session ->
                    session
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                }

        return http.build()
    }
}
