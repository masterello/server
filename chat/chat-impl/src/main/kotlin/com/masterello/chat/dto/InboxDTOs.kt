package com.masterello.chat.dto

import java.time.OffsetDateTime
import java.util.*

data class InboxItemDTO(
    val chatId: UUID,
    val lastMessageAt: OffsetDateTime?,
    val lastMessagePreview: String?
)

data class InboxSnapshotDTO(
    val items: List<InboxItemDTO>
)
