package com.masterello.chat.ws

import com.masterello.chat.presence.PresenceService
import com.masterello.chat.util.AuthUtil.getUser
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectedEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import java.time.Instant

@Component
class PresenceEvents(
    private val presenceService: PresenceService
) {
    private val log = KotlinLogging.logger {}

    @EventListener
    fun onSessionConnected(event: SessionConnectedEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val sessionId = accessor.sessionId ?: return
        val user = getUser(accessor) ?: return
        presenceService.onConnect(user.userId, sessionId, Instant.now())
        log.debug { "Presence connected user=${user.userId} session=$sessionId" }
    }

    @EventListener
    fun onSessionDisconnected(event: SessionDisconnectEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val sessionId = accessor.sessionId ?: return
        val user = getUser(accessor) ?: return
        presenceService.onDisconnect(user.userId, sessionId, Instant.now())
        log.debug { "Presence disconnected user=${user.userId} session=$sessionId" }
    }
}
