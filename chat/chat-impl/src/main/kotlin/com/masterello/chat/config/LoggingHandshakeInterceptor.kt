package com.masterello.chat.config

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

private val log = KotlinLogging.logger {}

class LoggingHandshakeInterceptor : HandshakeInterceptor {
    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {
        val servletReq = (request as? ServletServerHttpRequest)?.servletRequest
        val uri = request.uri
        val headers = request.headers
        val origin = headers.getFirst("Origin")
        val cookieHeaderCombined = headers["Cookie"]?.joinToString("; ") ?: headers.getFirst("Cookie")
        val cookiePresent = !cookieHeaderCombined.isNullOrBlank()
        val cookieKeys = if (!cookiePresent) emptyList() else cookieHeaderCombined!!.split(";")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { it.substringBefore("=") }
        val upgrade = headers.getFirst("Upgrade")
        val connection = headers.getFirst("Connection")
        val host = headers.getFirst("Host")
        val xff = headers.getFirst("X-Forwarded-For")
        val proto = headers.getFirst("X-Forwarded-Proto")
        val forwardedHost = headers.getFirst("X-Forwarded-Host")
        log.debug { "[WS-HS] before uri=$uri host=$host origin=$origin upgrade=$upgrade connection=$connection cookiePresent=$cookiePresent cookieKeys=$cookieKeys xff=$xff xfproto=$proto xfhost=$forwardedHost remote=${servletReq?.remoteAddr}" }
        return true
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {
        val uri = request.uri
        if (exception != null) {
            log.debug { "[WS-HS] after uri=$uri handshake failed: ${exception.message}" }
        } else {
            log.debug { "[WS-HS] after uri=$uri handshake succeeded" }
        }
    }
}

