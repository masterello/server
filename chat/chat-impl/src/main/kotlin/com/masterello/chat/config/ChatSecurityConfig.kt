package com.masterello.chat.config

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

@Configuration
@EnableWebSecurity
class ChatSecurityConfig(private val authService: AuthService) {

    @Bean
    @Throws(Exception::class)
    fun apiChatAuthFilter(http: HttpSecurity): SecurityFilterChain {
        val authFilter = AuthFilter(AntPathRequestMatcher("/**"), authService)

        http
                .securityMatcher("/api/chat/**", "/ws/chat/**")
                .csrf { it.disable() }
                .authorizeHttpRequests { auth ->
                    auth
                            .anyRequest().authenticated()
                }
                .addFilterBefore(authFilter, AnonymousAuthenticationFilter::class.java)
                .sessionManagement { session ->
                    session
                            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                }

        return http.build()
    }
}
