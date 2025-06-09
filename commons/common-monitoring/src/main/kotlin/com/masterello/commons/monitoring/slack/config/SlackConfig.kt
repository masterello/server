package com.masterello.commons.monitoring.slack.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
@ConditionalOnProperty(name = ["masterello.monitoring.channel"], havingValue = "slack", matchIfMissing = false)
class SlackConfig(private val slackProperties: SlackProperties) {

    @Bean
    @Qualifier("slackRestTemplate")
    fun getSlackRestTemplate(builder: RestTemplateBuilder): RestTemplate {
        return builder
                .basicAuthentication(slackProperties.clientId, slackProperties.clientSecret)
                .build();
    }
}