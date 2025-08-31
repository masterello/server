package com.masterello.chat.ws

import com.masterello.chat.dto.ChatMessageDTO
import com.masterello.chat.service.ChatMessageService
import com.masterello.chat.util.AuthUtil.getUser
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Controller
import java.util.UUID

@Controller
class ChatMessagingController(
    private val chatMessageService: ChatMessageService
) {
    
    private val log = KotlinLogging.logger {}

    @MessageMapping("/sendMessage/{chatId}")
    @SendTo("/topic/messages/{chatId}")
    fun sendMessage(
        message: String,
        @DestinationVariable chatId: UUID,
        accessor: StompHeaderAccessor
    ): ChatMessageDTO {
        val user = getUser(accessor)?: throw IllegalStateException("No authentication data found in session")
        
        log.info { "User ${user.userId} sending message to chat $chatId" }
        
        // Validate message content
        val trimmedMessage = message.trim()
        require(trimmedMessage.isNotEmpty()) { "Message cannot be empty" }
        require(trimmedMessage.length <= 1000) { "Message too long (max 1000 characters)" }
        
        // Persist message and update chat denorm in one transaction handled by the service
        return chatMessageService.saveMessageAndUpdateChat(chatId, trimmedMessage, user.userId)
    }
}
