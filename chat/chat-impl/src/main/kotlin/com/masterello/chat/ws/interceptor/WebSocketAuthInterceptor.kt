package com.masterello.chat.ws.interceptor

import com.masterello.auth.data.AuthData
import com.masterello.chat.exceptions.ChatNotFoundException
import com.masterello.chat.repository.ChatRepository
import com.masterello.commons.security.exception.UnauthorisedException
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.SimpMessageType
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Component
import java.util.*

@Component
class WebSocketAuthInterceptor(private val chatRepository: ChatRepository) : ChannelInterceptor {

    private val chatDestinationPattern: Regex = Regex("^/(topic/messages|ws/chat)/([a-zA-Z0-9-]+)$")

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = StompHeaderAccessor.wrap(message)

        if (SimpMessageType.SUBSCRIBE == accessor.messageType) {
            val chatId = accessor.destination?.let { extractChatId(it) }
            if (chatId != null) {
                checkPermissionsToConnectToChat(accessor, chatId)
            }
        }

        return message
    }

    fun extractChatId(destination: String): UUID? {
        val matchResult = chatDestinationPattern.matchEntire(destination)
        return matchResult?.groups?.get(2)?.value?.let(UUID::fromString)
    }

    private fun checkPermissionsToConnectToChat(accessor: StompHeaderAccessor, chatId: UUID) {
        val chat = chatRepository.findById(chatId)
                .orElseThrow { ChatNotFoundException("Chat $chatId not found") }
        val user = getUser(accessor)

        if (chat.userId != user.userId && chat.workerId != user.userId) {
            throw UnauthorisedException("User ${user.userId} is not authorized to connect to chat $chatId")
        }
    }

    private fun getUser(accessor: StompHeaderAccessor): AuthData {
        val sessionAttributes = accessor.sessionAttributes
        return sessionAttributes!!["SECURITY_CONTEXT"] as AuthData
    }
}
