package com.masterello.chat.dto

import java.time.OffsetDateTime
import java.util.*

data class ReadReceiptDTO(
    val chatId: UUID,
    val readerId: UUID,
    val readAt: OffsetDateTime,
    val messageIds: List<UUID>? = null,
    val visibleUpTo: OffsetDateTime? = null
)