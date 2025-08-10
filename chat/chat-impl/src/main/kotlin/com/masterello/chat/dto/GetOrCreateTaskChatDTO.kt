package com.masterello.chat.dto

import java.util.*

data class GetOrCreateTaskChatDTO(
    val workerId: UUID,
    val taskId: UUID,
)
