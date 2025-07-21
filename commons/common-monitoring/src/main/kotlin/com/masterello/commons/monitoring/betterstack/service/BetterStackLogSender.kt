package com.masterello.commons.monitoring.betterstack.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.masterello.commons.monitoring.LogDto
import com.masterello.commons.monitoring.LogSender
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
@ConditionalOnProperty(name = ["masterello.monitoring.log.channel"], havingValue = "better-stack", matchIfMissing = false)
class BetterStackLogSender(@Qualifier("betterStackRestTemplate") private val restTemplate: RestTemplate,
                           private val objectMapper: ObjectMapper) : LogSender {


    override fun sendLog(log: LogDto) {
        val json = objectMapper.writeValueAsString(log)

        restTemplate.postForEntity("/", json, String::class.java)
    }
}