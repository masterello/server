package com.masterello.file.configuration

import com.masterello.commons.security.filter.AuthFilter
import kotlin.jvm.Throws
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
open class FileSecurityConfig(private val authFilter: AuthFilter) {

    @Bean
    @Throws(Exception::class)
    open fun apiFileAuthFilter(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher(AntPathRequestMatcher("/api/files/**"))
            .csrf { it.disable() }
            .authorizeHttpRequests { auth -> auth
                .requestMatchers("/api/files/{userUuid}/images", "/api/files/{userUuid}/thumbnails",
                    "/api/files/{userUuid}", "/api/files/bulkSearch").permitAll()
                .anyRequest().authenticated() }
            .addFilterBefore(authFilter, AnonymousAuthenticationFilter::class.java)
            .sessionManagement { session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

        return http.build()
    }
}
