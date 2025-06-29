package com.masterello.ai.config

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@EnableConfigurationProperties(AiConfigProperties::class)
class AiConfig {

    @Bean("openAiWebClient")
    fun openAiWebClient(properties: AiConfigProperties): WebClient {
        return WebClient.builder()
                .baseUrl(properties.endpoint)
                .defaultHeader("Authorization", "Bearer ${properties.accessKey}")
                .build()
    }
}