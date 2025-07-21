package com.masterello.commons.monitoring

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(name = ["masterello.monitoring.log.channel"], havingValue = "none", matchIfMissing = true)
class NoOpLogSender : LogSender {

    override fun sendLog(log: LogDto) {
        // No operation; this method intentionally does nothing
    }
}