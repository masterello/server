package com.masterello.monitoring

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["masterello.monitoring.channel"], havingValue = "none", matchIfMissing = true)
class NoOpAlertSender : AlertSender {
    override fun sendAlert(message: AlertMessage) {
        // No operation; this method intentionally does nothing
    }
}