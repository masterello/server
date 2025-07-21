package com.masterello.commons.monitoring.betterstack.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding


@ConfigurationProperties(prefix = "masterello.monitoring.betterstack")
@ConditionalOnProperty(name = ["masterello.monitoring.log.channel"], havingValue = "better-stack", matchIfMissing = false)
data class BetterStackProperties @ConstructorBinding constructor(
        val apiKey: String,
        val endpoint: String,
)
