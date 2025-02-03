package com.masterello.config;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.Module;
import com.masterello.commons.security.serialization.AuthBasedSerializerModifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public Module customJacksonModule() {
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new AuthBasedSerializerModifier());
        return module;
    }
}