package com.masterello.chat.dto

import java.time.OffsetDateTime

data class ChatHistoryDTO(
        val messages: List<ChatMessageDTO>,
        val nextCursor: OffsetDateTime?,
        val hasMore: Boolean
)