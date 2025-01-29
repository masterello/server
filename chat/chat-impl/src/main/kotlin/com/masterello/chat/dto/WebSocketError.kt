package com.masterello.chat.dto

data class WebSocketError(val errorCode: WebSocketErrorCode, val errorMessage: String?)


enum class WebSocketErrorCode {
    UNAUTHORIZED,
    CHAT_NOT_FOUND,
    UNKNOWN_ERROR
}