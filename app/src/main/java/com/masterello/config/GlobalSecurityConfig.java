package com.masterello.config;

import com.masterello.monitoring.filter.UncaughtExceptionLoggingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import jakarta.servlet.Filter;

@Configuration
@RequiredArgsConstructor
public class GlobalSecurityConfig {

    private final UncaughtExceptionLoggingFilter loggingFilter;

    @Bean
    public FilterRegistrationBean<Filter> globalFilterRegistration() {
        FilterRegistrationBean<Filter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(loggingFilter);
        registrationBean.setOrder(0);
        registrationBean.addUrlPatterns("/*"); // Apply to all URL patterns
        return registrationBean;
    }
}
