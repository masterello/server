package com.masterello.ai.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.masterello.ai.service.AiService
import com.masterello.ai.service.DefaultAiService
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
@ConditionalOnProperty("masterello.ai.enabled")
class AiConfig {

    @Bean("openAiWebClient")
    fun openAiWebClient(properties: AiConfigProperties): WebClient {
        return WebClient.builder()
                .baseUrl(properties.endpoint)
                .defaultHeader("Authorization", "Bearer ${properties.accessKey}")
                .build()
    }

    @Bean
    fun aiService(@Qualifier("openAiWebClient") openAIWebClient: WebClient,
                  aiConfigProperties: AiConfigProperties,
                  objectMapper: ObjectMapper): AiService {
        return DefaultAiService(openAIWebClient, aiConfigProperties, objectMapper)
    }
}