package com.masterello.chat.dto

import java.time.OffsetDateTime
import java.util.*

data class MarkReadRequest(
    val visibleUpTo: OffsetDateTime? = null,
    val messageIds: List<UUID>? = null
)