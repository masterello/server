package com.masterello.category.configuration

import kotlin.jvm.Throws;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher

@Configuration
@EnableWebSecurity
open class CategorySecurityConfig {

    @Bean
    @Throws(Exception::class)
    open fun apiCategoryAuthFilter(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher(AntPathRequestMatcher("/api/categories/**"))
            .csrf { it.disable() }
            .authorizeHttpRequests { auth -> auth
                .anyRequest().permitAll() }
            .sessionManagement { session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) }

        return http.build()
    }
}
