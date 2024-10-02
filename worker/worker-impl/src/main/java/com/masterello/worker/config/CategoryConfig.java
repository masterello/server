package com.masterello.worker.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CategoryConfig {

    @Bean
    @Qualifier("categoryRestTemplate")
    public RestTemplate categoryRestTemplate() {
        return new RestTemplate();
    }
}
