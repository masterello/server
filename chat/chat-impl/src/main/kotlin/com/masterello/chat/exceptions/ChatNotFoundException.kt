package com.masterello.chat.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.NOT_FOUND)
class ChatNotFoundException(message: String) : RuntimeException(message)