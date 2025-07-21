package com.masterello.commons.monitoring.logger

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.ILoggingEvent
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
    fun addAppenderToMasterelloLogger() {
        this.start()
        val rootLogger = LoggerFactory.getLogger("root") as Logger
        rootLogger.addAppender(this)
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

