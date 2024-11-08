package com.masterello.chat.ws.interceptor

import com.masterello.commons.security.data.MasterelloAuthentication
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor

@Component
class AuthHandshakeInterceptor : HandshakeInterceptor {

    override fun beforeHandshake(request: ServerHttpRequest, response: ServerHttpResponse, wsHandler: WebSocketHandler, attributes: MutableMap<String, Any>): Boolean {
        val securityContext = SecurityContextHolder.getContext()
        attributes["SECURITY_CONTEXT"] =  (securityContext.authentication as MasterelloAuthentication).details
        return true
    }

    override fun afterHandshake(request: ServerHttpRequest, response: ServerHttpResponse, wsHandler: WebSocketHandler, exception: java.lang.Exception?) {
        // No action is needed here, so this method is left empty.
    }
}
