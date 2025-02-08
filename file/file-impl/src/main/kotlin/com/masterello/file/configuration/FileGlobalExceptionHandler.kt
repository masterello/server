package com.masterello.file.configuration

import com.masterello.file.exception.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice(basePackages = ["com.masterello.file"])
class FileGlobalExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundException(ex: NotFoundException): ResponseEntity<String> {
        return ResponseEntity(ex.message ?: "Resource not found", HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(FileTypeException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleFileTypeException(ex: FileTypeException): ResponseEntity<String> {
        return ResponseEntity(ex.message ?: "Unsupported file type provided to search bulk", HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(TaskNotProvidedException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleTaskNotProvidedException(ex: TaskNotProvidedException): ResponseEntity<String> {
        return ResponseEntity(ex.message ?: "Task uuid is not provided in the request", HttpStatus.BAD_REQUEST)
    }
}