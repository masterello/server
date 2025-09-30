package com.masterello.chat.dto

import java.time.OffsetDateTime
import java.util.*

data class ChatMessageDTO(
        val id: UUID,
        val chatId: UUID,
        val message: String,
        val createdBy: UUID,
        val createdAt: OffsetDateTime,
        val messageReadBy: List<MessageReadByDTO> = emptyList()
)
