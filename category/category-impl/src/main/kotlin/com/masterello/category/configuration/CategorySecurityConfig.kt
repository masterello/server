package com.masterello.category.configuration

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
open class CategorySecurityConfig(private val authService: AuthService) {

    @Bean
    @Throws(Exception::class)
    open fun apiCategoryAuthFilter(http: HttpSecurity): SecurityFilterChain {

        val matcherBuilder = PathPatternRequestMatcher.withDefaults()

        val protectedEndpoints: RequestMatcher = OrRequestMatcher(
            matcherBuilder.matcher(HttpMethod.POST, "/api/categories"),
            matcherBuilder.matcher(HttpMethod.PUT, "/api/categories/{id}"),
            matcherBuilder.matcher(HttpMethod.PUT, "/api/categories/{id}/activate")
        )

        val authFilter = AuthFilter(protectedEndpoints, authService)

        http
            .securityMatcher(matcherBuilder.matcher("/api/categories/**"))
            .csrf { it.disable() }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(protectedEndpoints).authenticated()
                    .anyRequest().permitAll()
            }
            .addFilterBefore(authFilter, AnonymousAuthenticationFilter::class.java)
            .sessionManagement { session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }

        return http.build()
    }
}
