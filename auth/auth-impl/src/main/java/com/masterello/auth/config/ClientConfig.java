package com.masterello.auth.config;

import com.masterello.auth.repository.MasterelloRegisteredClientRepository;
import com.masterello.auth.repository.RegisteredClientJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;

@Configuration
public class ClientConfig {

    @Autowired
    private MasterelloRegisteredClientRepository masterelloRegisteredClientRepository;

    @Bean
    public RegisteredClientRepository registeredClientRepository(TokenSettings tokenSettings) {
        return new RegisteredClientJpaRepository(masterelloRegisteredClientRepository, tokenSettings, clientSettings());
    }

    @Bean
    public ClientSettings clientSettings() {
        return ClientSettings.builder().build();
    }
}
