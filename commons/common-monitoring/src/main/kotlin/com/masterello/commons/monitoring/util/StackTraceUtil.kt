package com.masterello.commons.monitoring.util

import ch.qos.logback.classic.spi.IThrowableProxy
import ch.qos.logback.classic.spi.ThrowableProxyUtil
import org.apache.commons.lang3.StringUtils

object StackTraceUtil {
    fun format(proxy: IThrowableProxy?, maxLength: Int = 2000): String? {
        if (proxy == null) return null

        val full = ThrowableProxyUtil.asString(proxy)
        return StringUtils.abbreviate(full, maxLength)
    }
}
