package com.masterello.chat.service

import com.masterello.chat.dto.InboxItemDTO
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class ChatInboxEventListener(
    private val messagingTemplate: SimpMessagingTemplate
) {
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    fun onInboxEvent(event: ChatInboxEvent) {
        val dto = InboxItemDTO(
            chatId = event.chatId,
            lastMessageAt = event.lastMessageAt,
            lastMessagePreview = event.lastMessagePreview,
            senderId = event.senderId
        )
        event.recipients.forEach { uid ->
            messagingTemplate.convertAndSend("/topic/inbox/$uid", dto)
        }
    }
}
