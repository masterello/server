package com.masterello.auth.config;

import com.masterello.auth.service.AuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
public class AuthContractTestConfig {

    @Bean
    public AuthService authService() {
        return mock(AuthService.class);
    }
}
