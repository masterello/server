package com.masterello.chat.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
class ChatCreationValidationException(message: String): RuntimeException(message) {
}