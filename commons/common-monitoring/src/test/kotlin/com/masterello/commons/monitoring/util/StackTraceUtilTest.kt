package com.masterello.commons.monitoring.util

import ch.qos.logback.classic.spi.ThrowableProxy
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class StackTraceUtilTest {

    @Test
    fun `should return null when proxy is null`() {
        val result = StackTraceUtil.format(null)
        assertNull(result)
    }

    @Test
    fun `should format real thrown exception`() {
        val proxy = try {
            throw IllegalArgumentException("Boom!")
        } catch (ex: Exception) {
            ThrowableProxy(ex)
        }

        val result = StackTraceUtil.format(proxy, 2000)

        assertNotNull(result)
        assertTrue(result!!.contains("Boom!"))
        assertTrue(result.contains("java.lang.IllegalArgumentException"))
        assertTrue(result.contains("should format real thrown exception"))
    }

    @Test
    fun `should truncate long stack trace`() {
        val proxy = try {
            deepCall(100)
            null
        } catch (ex: Exception) {
            ThrowableProxy(ex)
        }

        val result = StackTraceUtil.format(proxy, 1000)

        assertNotNull(result)
        assertTrue(result!!.length <= 1000)
    }

    private fun deepCall(depth: Int) {
        if (depth == 0) throw RuntimeException("Deep failure")
        deepCall(depth - 1)
    }
}
