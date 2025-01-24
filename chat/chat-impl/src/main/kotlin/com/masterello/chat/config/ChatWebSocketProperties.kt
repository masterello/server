package com.masterello.chat.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "masterello.chat.ws")
data class ChatWebSocketProperties constructor(
        val clientHeartbeat: Long = 15000,
        val serverHeartbeat: Long = 15000,
        val taskSchedulerPoolSize: Int = 1
)