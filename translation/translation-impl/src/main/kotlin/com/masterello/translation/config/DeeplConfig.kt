package com.masterello.translation.config

import com.deepl.api.DeepLClient
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(DeeplConfigProperties::class)
class DeeplConfig(val properties: DeeplConfigProperties) {

    @Bean
    fun deeplClient(): DeepLClient {
        return DeepLClient(properties.apiKey)
    }
}