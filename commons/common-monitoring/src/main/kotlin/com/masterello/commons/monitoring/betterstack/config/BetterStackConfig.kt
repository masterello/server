package com.masterello.commons.monitoring.betterstack.config

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.web.client.RestTemplate

@Configuration
@ConditionalOnProperty(name = ["masterello.monitoring.log.channel"], havingValue = "better-stack", matchIfMissing = false)
class BetterStackConfig(private val betterStackProperties: BetterStackProperties) {

    @Bean
    @Qualifier("betterStackRestTemplate")
    fun getBetterStackRestTemplate(builder: RestTemplateBuilder): RestTemplate {
        val interceptor = ClientHttpRequestInterceptor { request, body, execution ->
            request.headers.setBearerAuth(betterStackProperties.apiKey)
            request.headers.contentType = MediaType.APPLICATION_JSON
            execution.execute(request, body)
        }

        return builder
                .additionalInterceptors(interceptor)
                .rootUri(betterStackProperties.endpoint)
                .build();
    }
}