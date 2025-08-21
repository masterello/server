package com.masterello.chat.dto

import java.time.Instant
import java.util.*

data class TypingEvent(
    val userId: UUID? = null,
    val started: Boolean = true,
    val at: Instant = Instant.now()
)
