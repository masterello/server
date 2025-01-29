package com.masterello.chat.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.OffsetDateTime
import java.util.*

data class ChatMessageDTO(
        val id: UUID,
        val chatId: UUID,
        val message: String,
        val createdBy: UUID,
        val createdAt: OffsetDateTime
)