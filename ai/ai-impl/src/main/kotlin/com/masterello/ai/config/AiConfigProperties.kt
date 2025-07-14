package com.masterello.ai.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "masterello.ai")
@ConditionalOnProperty("masterello.ai.enabled")
data class AiConfigProperties @ConstructorBinding constructor(
        val accessKey: String,
        val endpoint: String,
        val model: String
)