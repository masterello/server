package com.masterello.commons.monitoring.slack.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.masterello.commons.monitoring.AlertLevel
import com.masterello.commons.monitoring.AlertMessage
import com.masterello.commons.monitoring.AlertSender
import com.masterello.commons.monitoring.slack.config.SlackProperties
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
@ConditionalOnProperty(name = ["masterello.monitoring.channel"], havingValue = "slack", matchIfMissing = false)
class SlackAlertSender(@Qualifier("slackRestTemplate") private val restTemplate: RestTemplate,
                       private val slackConfig: SlackProperties,
                       private val objectMapper: ObjectMapper) : AlertSender {


    private final val maxStacktraceSize: Int = 2000

    override fun sendAlert(message: AlertMessage) {
        try {
            // Build Slack message payload with color attachment
            val payload = mapOf("attachments" to listOf(buildSlackMessage(message)))

            // Set headers
            val headers = HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
            }
            val entity = HttpEntity(objectMapper.writeValueAsString(payload), headers)

            // Send message
            restTemplate.postForEntity(slackConfig.endpoint, entity, String::class.java)
        } catch (e: Exception) {
            println("Error sending message to Slack: ${e.message}")
        }
    }

    private fun buildSlackMessage(message: AlertMessage): Map<String, Any> {
        val color = getColor(message.level)
        val icon = getIcon(message.level)

        // Main message content with color and blocks
        val blocks = mutableListOf(
                mapOf(
                        "type" to "section",
                        "text" to mapOf("type" to "mrkdwn", "text" to "$icon *${message.level.name.uppercase()}* Alert")
                ),
                mapOf("type" to "divider"),
                mapOf(
                        "type" to "section",
                        "text" to mapOf("type" to "mrkdwn", "text" to message.message)
                )
        )

        // Add stack trace as a separate block if present
        message.stackTrace?.let { stackTrace ->
            blocks.add(
                    mapOf(
                            "type" to "section",
                            "text" to mapOf("type" to "mrkdwn", "text" to "```${StringUtils.abbreviate(stackTrace, maxStacktraceSize)}```")
                    )
            )
        }

        return mapOf(
                "color" to color,
                "blocks" to blocks
        )
    }


    private fun getColor(alertLevel: AlertLevel): String {
        return when (alertLevel) {
            AlertLevel.ERROR -> "#FF0000"
            AlertLevel.WARN -> "#FFA500"
            AlertLevel.INFO -> "#00FFA5"
        }
    }

    private fun getIcon(alertLevel: AlertLevel): String {
        return when (alertLevel) {
            AlertLevel.ERROR -> ":this-is-fine:"
            AlertLevel.WARN -> ":warning:"
            AlertLevel.INFO -> ":dove_of_peace:"
        }
    }
}