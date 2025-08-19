package com.masterello.chat.ws.interceptor

import com.masterello.auth.data.AuthData
import com.masterello.chat.security.ChatSecurityExpressions
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Component
import java.util.*

@Component
class WebSocketAuthInterceptor(
    private val chatSecurityExpressions: ChatSecurityExpressions
) : ChannelInterceptor {
    
    private val log = KotlinLogging.logger {}
    // Only allow broker destinations for subscriptions: /topic/messages/<uuid>
    private val chatDestinationPattern: Regex = Regex("^/topic/messages/([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})$")

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = StompHeaderAccessor.wrap(message)

        if (SimpMessageType.SUBSCRIBE == accessor.messageType) {
            val chatId = accessor.destination?.let { extractChatId(it) }
            if (chatId != null) {
                checkPermissionsToSubscribeToChat(accessor, chatId)
            }
        }

        return message
    }

    fun extractChatId(destination: String): UUID? {
        return try {
            val matchResult = chatDestinationPattern.matchEntire(destination)
            matchResult?.groups?.get(1)?.value?.let { UUID.fromString(it) }
        } catch (ex: IllegalArgumentException) {
            log.warn { "Invalid UUID format in destination: $destination" }
            null
        }
    }

    private fun checkPermissionsToSubscribeToChat(accessor: StompHeaderAccessor, chatId: UUID) {
        val user = getUser(accessor)
        
        // Use centralized authorization logic with userId extracted from session attributes
        if (!chatSecurityExpressions.canSubscribeToChat(user.userId, chatId)) {
            throw org.springframework.security.access.AccessDeniedException(
                "User ${user.userId} is not authorized to subscribe to chat $chatId"
            )
        }
        
        log.debug { "WebSocket subscription authorized for user ${user.userId} to chat $chatId" }
    }

    private fun getUser(accessor: StompHeaderAccessor): AuthData {
        val sessionAttributes = accessor.sessionAttributes
            ?: throw IllegalStateException("No session attributes found")
        
        return sessionAttributes["SECURITY_CONTEXT"] as? AuthData
            ?: throw IllegalStateException("No authentication data found in session")
    }
}
