package com.masterello.commons.monitoring.grafana.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.masterello.commons.monitoring.LogDto
import com.masterello.commons.monitoring.LogSender
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.time.Instant

@Service
@ConditionalOnProperty(name = ["masterello.monitoring.log.channel"], havingValue = "grafana")
class GrafanaLogSender(
        @Qualifier("grafanaRestTemplate") private val restTemplate: RestTemplate,
        private val objectMapper: ObjectMapper
) : LogSender {
    private val logger = KotlinLogging.logger {}

    override fun sendLog(log: LogDto) {
        val timestampNs = Instant.parse(log.timestamp).toEpochMilli().toString() + "000000"

        val payload = mapOf(
                "streams" to listOf(
                        mapOf(
                                "stream" to mapOf(
                                        "job" to "masterello-server",
                                        "level" to log.level,
                                        "env" to "production"
                                ),
                                "values" to listOf(listOf(timestampNs, objectMapper.writeValueAsString(log)))
                        )
                )
        )
        logger.info { payload }
        restTemplate.postForEntity("/loki/api/v1/push", payload, String::class.java)
    }
}
