package com.masterello.chat.ws

import com.masterello.auth.data.AuthData
import com.masterello.chat.domain.Message
import com.masterello.chat.dto.ChatMessageDTO
import com.masterello.chat.mapper.MessageMapper
import com.masterello.chat.repository.MessageRepository
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Controller
import java.util.*


@Controller
class ChatMessagingController(
        private val messageRepository: MessageRepository,
        private val messageMapper: MessageMapper) {

    @MessageMapping("/sendMessage/{chatId}")
    @SendTo("/topic/messages/{chatId}")
    fun sendMessage(message: String, @DestinationVariable chatId: UUID, accessor: StompHeaderAccessor): ChatMessageDTO {
        val user = getUser(accessor)
        val saved = messageRepository.save(Message(
                chatId = chatId,
                message = message,
                createdBy = user.userId,
        ))
        return messageMapper.toDto(saved)
    }

    private fun getUser(accessor: StompHeaderAccessor): AuthData {
        val sessionAttributes = accessor.sessionAttributes
        return sessionAttributes!!["SECURITY_CONTEXT"] as AuthData
    }
}