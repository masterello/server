package com.masterello.chat.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(value = HttpStatus.NOT_FOUND)
class TaskNotFoundException(message: String): RuntimeException(message) {
}