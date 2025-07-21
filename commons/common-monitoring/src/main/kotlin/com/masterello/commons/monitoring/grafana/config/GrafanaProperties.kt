package com.masterello.commons.monitoring.grafana.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "masterello.monitoring.grafana")
@ConditionalOnProperty(name = ["masterello.monitoring.log.channel"], havingValue = "grafana", matchIfMissing = false)
data class GrafanaProperties @ConstructorBinding constructor(
        val apiKey: String,
        val endpoint: String,
        val instanceId: String
)

