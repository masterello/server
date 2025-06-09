package com.masterello.monitoring

interface AlertSender {
    fun sendAlert(message: AlertMessage)
}

data class AlertMessage(val level: AlertLevel, val message: String, val stackTrace: String? = null)

enum class AlertLevel {
    ERROR,
    WARN,
    INFO
}