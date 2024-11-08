package com.masterello.chat.dto

import java.util.*

data class ChatDTO(
        val id: UUID,
        val taskId: UUID,
        val userId: UUID,
        val workerId: UUID,
        val userName: String,
        val workerName: String
)