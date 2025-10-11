package com.masterello.commons.monitoring.logger

import ch.qos.logback.classic.AsyncAppender
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.Appender
import ch.qos.logback.core.AppenderBase
import com.masterello.commons.monitoring.LogDto
import com.masterello.commons.monitoring.LogSender
import com.masterello.commons.monitoring.util.StackTraceUtil
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant


@Component
class ExternalLogAppender(
        private val logSender: LogSender
) : AppenderBase<ILoggingEvent>() {

    @PostConstruct
    fun addAppenderToRootLogger() {
        val rootLogger = LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger
        val loggerContext = rootLogger.loggerContext

        if (rootLogger.getAppender("ExternalLogAsync") != null) return

        this.context = loggerContext
        this.start()

        val async = AsyncAppender().apply {
            name = "ExternalLogAsync"
            context = loggerContext

            queueSize = 8192
            discardingThreshold = 0
            isNeverBlock = true
        }

        async.addAppender(this as Appender<ILoggingEvent>)
        async.start()
        rootLogger.addAppender(async)
    }

    override fun append(eventObject: ILoggingEvent) {
        try {
            val log = LogDto(
                    level = eventObject.level.toString(),
                    message = eventObject.formattedMessage,
                    logger = eventObject.loggerName,
                    timestamp = Instant.ofEpochMilli(eventObject.timeStamp).toString(),
                    thread = eventObject.threadName,
                    exception = StackTraceUtil.format(eventObject.throwableProxy),
            )
            logSender.sendLog(log)
        } catch (ex: Exception) {
            System.err.println("ExternalLogAppender error: ${ex.message}")
        }
    }
}

