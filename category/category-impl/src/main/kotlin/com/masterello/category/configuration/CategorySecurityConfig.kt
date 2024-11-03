package com.masterello.category.configuration

import com.masterello.commons.security.filter.AuthFilter
import kotlin.jvm.Throws;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.OrRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

@Configuration
@EnableWebSecurity
open class CategorySecurityConfig(private val authFilter: AuthFilter) {

    @Bean
    @Throws(Exception::class)
    open fun apiCategoryAuthFilter(http: HttpSecurity): SecurityFilterChain {

        val protectedEndpoints: RequestMatcher = OrRequestMatcher(
                AntPathRequestMatcher("/api/categories", "POST"),
                AntPathRequestMatcher("/api/categories/{id}", "PUT"),
                AntPathRequestMatcher("/api/categories/{id}/activate", "PUT")
        )

        http
                .securityMatcher(AntPathRequestMatcher("/api/categories/**"))
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
