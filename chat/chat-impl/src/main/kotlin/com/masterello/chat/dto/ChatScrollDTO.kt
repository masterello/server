package com.masterello.chat.dto

data class ChatScrollDTO(
    val items: List<ChatDTO>,
    val nextCursor: java.time.OffsetDateTime?,
    val hasMore: Boolean
)
