package com.masterello.chat.service

import java.time.OffsetDateTime
import java.util.UUID

/**
 * Domain event published inside a transaction and delivered to listeners AFTER_COMMIT.
 */
data class ChatInboxEvent(
    val chatId: UUID,
    val recipients: List<UUID>,
    val lastMessageAt: OffsetDateTime?,
    val lastMessagePreview: String?,
    val senderId: UUID?
)
