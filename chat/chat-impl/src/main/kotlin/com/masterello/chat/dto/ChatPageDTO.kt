package com.masterello.chat.dto

data class ChatPageDTO(
    val items: List<ChatDTO>,
    val page: Int,
    val size: Int,
    val totalPages: Int,
    val totalElements: Long,
    val hasNext: Boolean,
    val hasPrevious: Boolean
)
