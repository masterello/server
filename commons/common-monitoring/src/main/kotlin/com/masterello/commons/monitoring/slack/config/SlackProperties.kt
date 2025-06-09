package com.masterello.commons.monitoring.slack.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding


@ConfigurationProperties(prefix = "masterello.monitoring.slack")
@ConditionalOnProperty(name = ["masterello.monitoring.channel"], havingValue = "slack", matchIfMissing = false)
data class SlackProperties @ConstructorBinding constructor(
        val clientId: String,
        val clientSecret: String,
        val endpoint: String,
)
