package com.masterello.file.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "masterello.file.spaces")
data class DigitalOceanProperties @ConstructorBinding constructor(
        val accessKey: String,
        val secretKey: String,
        val endpoint: String,
        val region: String
)