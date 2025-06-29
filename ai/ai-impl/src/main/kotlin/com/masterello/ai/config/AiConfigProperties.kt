package com.masterello.ai.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@ConfigurationProperties(prefix = "masterello.ai")
data class AiConfigProperties @ConstructorBinding constructor(
        val accessKey: String,
        val endpoint: String,
        val model: String
)