package com.masterello.chat.dto

import com.masterello.chat.domain.ChatType
import java.time.OffsetDateTime
import java.util.*

data class ChatDTO(
    val id: UUID,
    val userId: UUID,
    val workerId: UUID,
    val chatType: ChatType,
    val taskId: UUID? = null,
    val userName: String,
    val workerName: String,
    val createdAt: OffsetDateTime,
    val lastMessageAt: OffsetDateTime?,
    val lastMessagePreview: String?
)
