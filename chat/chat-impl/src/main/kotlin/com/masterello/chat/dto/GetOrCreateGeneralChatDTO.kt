package com.masterello.chat.dto

import java.util.*

data class GetOrCreateGeneralChatDTO(
    val workerId: UUID,
    val userId: UUID,
)
