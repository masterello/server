package com.masterello.file.configuration

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
import org.springframework.security.web.util.matcher.AntPathRequestMatcher
import org.springframework.security.web.util.matcher.NegatedRequestMatcher
import org.springframework.security.web.util.matcher.OrRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher

private const val uuidPattern = "[0-9a-fA-F\\-]{36}"

@Configuration
@EnableWebSecurity
class FileSecurityConfig(private val authService: AuthService) {

    @Bean
    @Throws(Exception::class)
    fun apiFileAuthFilter(http: HttpSecurity): SecurityFilterChain {

        val privateEndpoints: RequestMatcher = OrRequestMatcher(
                AntPathRequestMatcher("/api/files/upload", HttpMethod.POST.name()),                  // Authenticated: Upload file
                AntPathRequestMatcher("/api/files/{userUuid:$uuidPattern}/confirm", HttpMethod.POST.name()),                  // Authenticated: Confirm file uploading
                AntPathRequestMatcher("/api/files/{userUuid:$uuidPattern}/{fileUuid:$uuidPattern}", HttpMethod.DELETE.name()) // Authenticated: Delete file
        )
        val authFilter = AuthFilter(privateEndpoints, authService)

        http
            .securityMatcher(AntPathRequestMatcher("/api/files/**"))
            .csrf { it.disable() }
            .authorizeHttpRequests { auth -> auth
                .requestMatchers(NegatedRequestMatcher(privateEndpoints)).permitAll()
                .anyRequest().authenticated() }
            .addFilterBefore(authFilter, AnonymousAuthenticationFilter::class.java)
            .sessionManagement { session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

        return http.build()
    }
}
