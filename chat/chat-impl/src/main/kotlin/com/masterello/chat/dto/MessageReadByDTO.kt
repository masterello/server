package com.masterello.chat.dto

import java.time.OffsetDateTime
import java.util.*

data class MessageReadByDTO(
    val readerId: UUID,
    val readAt: OffsetDateTime
)