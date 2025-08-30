package com.masterello.chat.exceptions

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.CONFLICT)
class ChatAlreadyExistsException(message: String) : RuntimeException(message)