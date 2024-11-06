package com.masterello.monitoring.logger

import ch.qos.logback.classic.Level.ERROR
import ch.qos.logback.classic.Level.WARN
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import com.masterello.monitoring.AlertLevel
import com.masterello.monitoring.AlertMessage
import com.masterello.monitoring.AlertSender
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AlertAppender(private val alertSender: AlertSender) : AppenderBase<ILoggingEvent>() {

    @PostConstruct
    fun addAppenderToMasterelloLogger() {
        this.start()
        val rootLogger = LoggerFactory.getLogger("com.masterello") as Logger
        rootLogger.addAppender(this)
    }

    override fun append(event: ILoggingEvent) {
        val level = when (event.level) {
            ERROR -> AlertLevel.ERROR
            WARN -> AlertLevel.WARN
            else -> return
        }
        val stackTrace = getStackTrace(event)
        alertSender.sendAlert(AlertMessage(level, event.formattedMessage, stackTrace))
    }

    private fun getStackTrace(event: ILoggingEvent) : String? {
        return event.throwableProxy?.let { throwableProxy ->
            throwableProxy.cause.stackTraceElementProxyArray.joinToString("\n") { it.stackTraceElement.toString() }
        }
    }
}