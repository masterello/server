package com.masterello.auth.config;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterello.auth.service.JpaOAuth2AuthorizationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;
import java.util.function.Supplier;

@Configuration
@EnableScheduling
@EnableTransactionManagement
public class AuthorisationServerConfig {

    @Bean
    public Supplier<ObjectMapper> authServiceObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        ClassLoader classLoader = JpaOAuth2AuthorizationService.class.getClassLoader();
        List<Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
        objectMapper.registerModules(securityModules);
        objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());

        return () -> objectMapper;
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .tokenRevocationEndpoint("/oauth2/logout")
                .build();
    }

}
