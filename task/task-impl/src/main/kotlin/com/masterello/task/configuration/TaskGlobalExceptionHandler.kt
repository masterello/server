package com.masterello.task.configuration

import com.masterello.task.exception.BadRequestException
import com.masterello.task.exception.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice(basePackages = ["com.masterello.task"])
class TaskGlobalExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundException(ex: NotFoundException): ResponseEntity<String> {
        return ResponseEntity(ex.message ?: "Resource not found", HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(BadRequestException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleNotFoundException(ex: BadRequestException): ResponseEntity<String> {
        return ResponseEntity(ex.message ?: "Invalid request", HttpStatus.BAD_REQUEST)
    }
}