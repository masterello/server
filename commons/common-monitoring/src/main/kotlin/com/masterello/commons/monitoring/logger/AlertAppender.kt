package com.masterello.commons.monitoring.logger

import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.Level.ERROR
import ch.qos.logback.classic.Level.WARN
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.AppenderBase
import com.masterello.commons.monitoring.AlertLevel
import com.masterello.commons.monitoring.AlertMessage
import com.masterello.commons.monitoring.AlertSender
import com.masterello.commons.monitoring.util.StackTraceUtil
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class AlertAppender(private val alertSender: AlertSender) : AppenderBase<ILoggingEvent>() {

    @PostConstruct
    fun addAppenderToMasterelloLogger() {
        val masterelloLogger = LoggerFactory.getLogger("com.masterello") as Logger
        val loggerContext = masterelloLogger.loggerContext

        if (masterelloLogger.getAppender("AlertLogAsync") != null) return

        this.context = loggerContext
        this.start()

        val async = AsyncAppender().apply {
            name = "AlertLogAsync"
            context = loggerContext

            queueSize = 8192
            discardingThreshold = 0
            isNeverBlock = true
        }

        async.addAppender(this as Appender<ILoggingEvent>)
        async.start()
        masterelloLogger.addAppender(async)
    }

    override fun append(event: ILoggingEvent) {
        val level = when (event.level) {
            ERROR -> AlertLevel.ERROR
            WARN -> AlertLevel.WARN
            else -> return
        }
        alertSender.sendAlert(AlertMessage(level, event.formattedMessage, StackTraceUtil.format(event.throwableProxy)))
    }
}