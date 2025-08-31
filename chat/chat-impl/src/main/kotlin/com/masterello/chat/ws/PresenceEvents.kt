package com.masterello.chat.ws

import com.masterello.chat.dto.PresenceStatus
import com.masterello.chat.util.AuthUtil.getUser
import com.masterello.chat.ws.session.SessionRegistry
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Component
class PresenceEvents(
    private val sessionRegistry: SessionRegistry,
    private val messagingTemplate: SimpMessagingTemplate
) {
    private val log = KotlinLogging.logger {}

    @EventListener
    fun onSessionConnect(event: SessionConnectEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val sessionId = accessor.sessionId ?: return
        val user = getUser(accessor) ?: return
        sessionRegistry.store(sessionId, user.userId)
        // Publish user presence snapshot to user-scoped topic
        publishUserPresence(user.userId)
        log.debug { "Presence connected user=${user.userId} session=$sessionId" }
    }

    @EventListener
    fun onSessionDisconnected(event: SessionDisconnectEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val sessionId = accessor.sessionId ?: return
        // Resolve userId before removing the session
        val userId = sessionRegistry.remove(sessionId)
        if (userId != null) {
            // Publish updated user presence (reflects ONLINE if other sessions remain, else OFFLINE)
            publishUserPresence(userId)
            log.debug { "Presence disconnected user=$userId session=$sessionId" }
        } else {
            log.debug { "Presence disconnected session=$sessionId (user unknown)" }
        }
    }
    private fun publishUserPresence(userId: java.util.UUID) {
        val online = sessionRegistry.anyOnline(userId)
        val dto = com.masterello.chat.dto.UserPresenceDTO(
            userId = userId,
            status = if (online) PresenceStatus.ONLINE else PresenceStatus.OFFLINE
        )
        messagingTemplate.convertAndSend("/topic/presence/user/$userId", dto)
    }
}
