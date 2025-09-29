package com.masterello.chat.dto

import java.util.*

data class CreateTaskChatDTO(
    val workerId: UUID,
    val taskId: UUID,
)
