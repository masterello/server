package com.masterello.chat.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "masterello.chat.ws")
data class ChatWebSocketProperties (
        val clientHeartbeat: Long = 15000,
        val serverHeartbeat: Long = 15000,
        val taskSchedulerPoolSize: Int = 1,
        val inboundThreadPool: ThreadPoolConfig = ThreadPoolConfig(),
        val outboundThreadPool: ThreadPoolConfig = ThreadPoolConfig()
)

data class ThreadPoolConfig(
        val coreSize: Int = 10,
        val maxSize: Int = 50,
        val queueCapacity: Int = 100,
        val keepAliveSeconds: Int = 60
)