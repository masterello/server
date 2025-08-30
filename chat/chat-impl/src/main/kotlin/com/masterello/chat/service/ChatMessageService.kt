package com.masterello.chat.service

import com.masterello.chat.domain.Message
import com.masterello.chat.dto.ChatMessageDTO
import com.masterello.chat.mapper.MessageMapper
import com.masterello.chat.repository.ChatRepository
import com.masterello.chat.repository.MessageRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.UUID

@Service
class ChatMessageService(
    private val messageRepository: MessageRepository,
    private val chatRepository: ChatRepository,
    private val messageMapper: MessageMapper
) {
    private val log = KotlinLogging.logger {}

    @Transactional
    fun saveMessageAndUpdateChat(chatId: UUID, messageText: String, createdBy: UUID): ChatMessageDTO {
        val saved = messageRepository.saveAndFlush(
            Message(
                chatId = chatId,
                message = messageText,
                createdBy = createdBy
            )
        )
        val preview = if (messageText.length > 180) messageText.substring(0, 180) else messageText
        val createdAt: OffsetDateTime = saved.createdAt ?: OffsetDateTime.now()
        // Atomic denorm update ("keep the max")
        chatRepository.updateLastMessage(chatId, createdAt, preview)
        log.info { "saved: $saved" }
        return messageMapper.toDto(saved)
    }
}
