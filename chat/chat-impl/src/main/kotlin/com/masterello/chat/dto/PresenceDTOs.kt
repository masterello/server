package com.masterello.chat.dto

import java.time.Instant
import java.util.*

enum class PresenceStatus { ONLINE, OFFLINE }

data class PresenceEntry(
    val userId: UUID,
    val status: PresenceStatus,
    val lastSeen: Instant?
)

data class PresenceSnapshotDTO(
    val chatId: UUID,
    val participants: List<PresenceEntry>
)
