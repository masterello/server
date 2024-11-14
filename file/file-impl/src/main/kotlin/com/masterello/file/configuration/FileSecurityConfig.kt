package com.masterello.file.configuration

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
import org.springframework.security.web.util.matcher.NegatedRequestMatcher
import org.springframework.security.web.util.matcher.OrRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

@Configuration
@EnableWebSecurity
open class FileSecurityConfig(private val authService: AuthService) {

    @Bean
    @Throws(Exception::class)
    open fun apiFileAuthFilter(http: HttpSecurity): SecurityFilterChain {
        val publicEndpoints: RequestMatcher = OrRequestMatcher(
                AntPathRequestMatcher("/api/files/{userUuid}/images"),
                AntPathRequestMatcher("/api/files/{userUuid}/thumbnails"),
                AntPathRequestMatcher("/api/files/{userUuid}"),
                AntPathRequestMatcher("/api/files/bulkSearch"),
        )
        val authFilter = AuthFilter(
                NegatedRequestMatcher(publicEndpoints), authService)

        http
            .securityMatcher(AntPathRequestMatcher("/api/files/**"))
            .csrf { it.disable() }
            .authorizeHttpRequests { auth -> auth
                .requestMatchers(publicEndpoints).permitAll()
                .anyRequest().authenticated() }
            .addFilterBefore(authFilter, AnonymousAuthenticationFilter::class.java)
            .sessionManagement { session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

        return http.build()
    }
}
