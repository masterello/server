package com.masterello.task.configuration

import com.masterello.auth.service.AuthService
import com.masterello.commons.security.filter.AuthFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher
import org.springframework.security.web.util.matcher.OrRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

@Configuration
@EnableWebSecurity
open class TaskSecurityConfig(private val authService: AuthService) {

    @Bean
    @Throws(Exception::class)
    open fun apiTaskAuthFilter(http: HttpSecurity): SecurityFilterChain {

        val matcherBuilder = PathPatternRequestMatcher.withDefaults()

        val protectedEndpoints: RequestMatcher = OrRequestMatcher(
                matcherBuilder.matcher(HttpMethod.GET, "/api/tasks/{taskUuid}"),
                matcherBuilder.matcher(HttpMethod.POST, "/api/tasks/user/{userUuid}/search"),
                matcherBuilder.matcher(HttpMethod.POST, "/api/tasks/worker/{workerUuid}/search"),
                matcherBuilder.matcher(HttpMethod.POST, "/api/tasks"),
                matcherBuilder.matcher(HttpMethod.POST, "/api/tasks/search"),
                matcherBuilder.matcher(HttpMethod.POST, "/api/tasks/worker/search"),
                matcherBuilder.matcher(HttpMethod.POST, "/api/tasks/{taskUuid}/update"),
                matcherBuilder.matcher(HttpMethod.POST, "/api/tasks/{taskUuid}/assign"),
                matcherBuilder.matcher(HttpMethod.POST, "/api/tasks/{taskUuid}/unassign"),
                matcherBuilder.matcher(HttpMethod.POST, "/api/tasks/{taskUuid}/reassign"),
                matcherBuilder.matcher(HttpMethod.POST, "/api/tasks/{taskUuid}/complete"),
                matcherBuilder.matcher(HttpMethod.POST, "/api/tasks/{taskUuid}/cancel"),
                matcherBuilder.matcher(HttpMethod.POST, "/api/tasks/worker-review"),
                matcherBuilder.matcher(HttpMethod.POST, "/api/tasks/user-review"),
                matcherBuilder.matcher(HttpMethod.POST, "/api/tasks/{taskUuid}/worker/{workerUuid}/confirm"),
        )

        val authFilter = AuthFilter(protectedEndpoints, authService)

        http
                .securityMatcher("/api/tasks/**", "/api/rating/**")
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
