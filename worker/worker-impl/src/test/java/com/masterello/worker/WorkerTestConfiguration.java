package com.masterello.worker;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.masterello.commons.security.serialization.AuthBasedSerializerModifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@ComponentScan(basePackages = {
        "com.masterello.worker",
        "com.masterello.user",
        "com.masterello.user.domain",
        "com.masterello.auth",
        "com.masterello.category",
        "com.masterello.ai",
        "com.masterello.commons"
})
@EnableAutoConfiguration
@EnableJpaAuditing
public class WorkerTestConfiguration {

    @Bean
    public Module customJacksonModule() {
        SimpleModule module = new SimpleModule();
        module.setSerializerModifier(new AuthBasedSerializerModifier());
        return module;
    }
}
