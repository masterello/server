package com.masterello.chat.service

import com.masterello.chat.domain.Chat
import com.masterello.chat.domain.Message
import com.masterello.chat.domain.MessageRead
import com.masterello.chat.domain.MessageReadId
import com.masterello.chat.dto.ChatMessageDTO
import com.masterello.chat.mapper.MessageMapper
import com.masterello.chat.repository.ChatRepository
import com.masterello.chat.repository.MessageReadRepository
import com.masterello.chat.repository.MessageRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class ChatMessageService(
        private val messageRepository: MessageRepository,
        private val chatRepository: ChatRepository,
        private val messageMapper: MessageMapper,
        private val applicationEventPublisher: org.springframework.context.ApplicationEventPublisher,
        private val messageReadRepository: MessageReadRepository
) {
    private val log = KotlinLogging.logger {}

    @Transactional
    fun saveMessageAndUpdateChat(chatId: UUID, messageText: String, createdBy: UUID): ChatMessageDTO {
        val message = saveMessage(chatId, messageText, createdBy)
        updateChatLastMessage(messageText, chatId, message)
        val chat = chatRepository.findById(chatId).orElseThrow()
        saveMessageRead(chat, message)
        publishInboxEvent(chat, message)

        log.info { "saved: $message" }
        return messageMapper.toDto(message, emptyList())
    }

    private fun publishInboxEvent(chat: Chat, message: Message) {
        try {
            applicationEventPublisher.publishEvent(
                    ChatInboxEvent(
                            chatId = chat.id!!,
                            recipients = listOf(chat.userId, chat.workerId),
                            lastMessageAt = message.createdAt,
                            lastMessagePreview = chat.lastMessagePreview,
                            senderId = message.createdBy
                    )
            )
        } catch (e: Exception) {
            log.warn(e) { "Failed to publish ChatInboxEvent for chat ${chat.id}" }
        }
    }

    private fun updateChatLastMessage(messageText: String, chatId: UUID, message: Message): String {
        val preview = if (messageText.length > 180) messageText.substring(0, 180) else messageText
        // Atomic denorm update ("keep the max")
        chatRepository.updateLastMessage(chatId, message.createdAt!!, preview)
        return preview
    }

    private fun saveMessageRead(chat: Chat, message: Message) {
        val recipientId: UUID = if (chat.userId == message.createdBy) chat.workerId else chat.userId
        messageReadRepository.save(
                MessageRead(
                        id = MessageReadId(messageId = message.id!!, recipientId = recipientId),
                        chatId = chat.id!!,
                        readAt = null,
                        createdAt = message.createdAt!!
                )
        )
    }

    private fun saveMessage(chatId: UUID, messageText: String, createdBy: UUID): Message =
            messageRepository.saveAndFlush(
                    Message(
                            chatId = chatId,
                            message = messageText,
                            createdBy = createdBy
                    )
            )
}
