package com.masterello.chat.dto

import java.util.*

data class GetOrCreateChatDTO(
        val taskId: UUID,
        val workerId: UUID
)