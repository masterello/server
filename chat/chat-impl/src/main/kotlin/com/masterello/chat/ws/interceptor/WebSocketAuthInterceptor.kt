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
    private val subscribeDestinationPattern: Regex = Regex("^/topic/messages/([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})$")
    // Authorize SEND frames to application destination: /ws/sendMessage/<uuid>
    private val sendDestinationPattern: Regex = Regex("^/ws/sendMessage/([a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12})$")

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = StompHeaderAccessor.wrap(message)

        when (accessor.messageType) {
            SimpMessageType.SUBSCRIBE -> {
                val chatId = accessor.destination?.let { extractChatIdForSubscribe(it) }
                if (chatId != null) {
                    checkPermissionsToSubscribeToChat(accessor, chatId)
                }
            }
            SimpMessageType.MESSAGE -> {
                val chatId = accessor.destination?.let { extractChatIdForSend(it) }
                if (chatId != null) {
                    checkPermissionsToSendToChat(accessor, chatId)
                }
            }
            else -> {}
        }

        return message
    }

    fun extractChatIdForSubscribe(destination: String): UUID? {
        return try {
            val matchResult = subscribeDestinationPattern.matchEntire(destination)
            matchResult?.groups?.get(1)?.value?.let { UUID.fromString(it) }
        } catch (ex: IllegalArgumentException) {
            log.warn { "Invalid UUID format in SUBSCRIBE destination: $destination" }
            null
        }
    }

    fun extractChatIdForSend(destination: String): UUID? {
        return try {
            val matchResult = sendDestinationPattern.matchEntire(destination)
            matchResult?.groups?.get(1)?.value?.let { UUID.fromString(it) }
        } catch (ex: IllegalArgumentException) {
            log.warn { "Invalid UUID format in SEND destination: $destination" }
            null
        }
    }

    private fun checkPermissionsToSubscribeToChat(accessor: StompHeaderAccessor, chatId: UUID) {
        val user = getUser(accessor)
        
        if (!chatSecurityExpressions.canSubscribeToChat(user.userId, chatId)) {
            throw org.springframework.security.access.AccessDeniedException(
                "User ${user.userId} is not authorized to subscribe to chat $chatId"
            )
        }
        
        log.debug { "WebSocket subscription authorized for user ${user.userId} to chat $chatId" }
    }

    private fun checkPermissionsToSendToChat(accessor: StompHeaderAccessor, chatId: UUID) {
        val user = getUser(accessor)

        if (!chatSecurityExpressions.canSendMessageToChat(user.userId, chatId)) {
            throw org.springframework.security.access.AccessDeniedException(
                "User ${user.userId} is not authorized to send messages to chat $chatId"
            )
        }

        log.debug { "WebSocket send authorized for user ${user.userId} to chat $chatId" }
    }

    private fun getUser(accessor: StompHeaderAccessor): AuthData {
        val sessionAttributes = accessor.sessionAttributes
            ?: throw IllegalStateException("No session attributes found")
        
        return sessionAttributes["SECURITY_CONTEXT"] as? AuthData
            ?: throw IllegalStateException("No authentication data found in session")
    }
}
