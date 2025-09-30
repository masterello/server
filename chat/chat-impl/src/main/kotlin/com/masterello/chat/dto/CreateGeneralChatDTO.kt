package com.masterello.chat.dto

import java.util.*

data class CreateGeneralChatDTO(
    val workerId: UUID,
    val userId: UUID,
)
