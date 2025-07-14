package com.masterello.translation.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding


@ConfigurationProperties(prefix = "masterello.translation.deepl")
data class DeeplConfigProperties @ConstructorBinding constructor(
        val apiKey: String,
)