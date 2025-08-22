package com.masterello.chat.dto

import java.util.*

enum class PresenceStatus { ONLINE, OFFLINE }

// Per-user presence payload for user-scoped topics
data class UserPresenceDTO(
    val userId: UUID,
    val status: PresenceStatus
)
