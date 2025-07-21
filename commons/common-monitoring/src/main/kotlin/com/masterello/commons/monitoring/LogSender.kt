package com.masterello.commons.monitoring

interface LogSender {
    fun sendLog(log: LogDto)
}

data class LogDto(
        val level: String,
        val message: String,
        val logger: String,
        val timestamp: String,
        val thread: String,
        val exception: String? = null,
)