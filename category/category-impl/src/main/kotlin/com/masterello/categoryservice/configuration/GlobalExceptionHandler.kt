package com.masterello.categoryservice.configuration

import com.masterello.categoryservice.exception.CategoryAlreadyExistsException
import com.masterello.categoryservice.exception.NotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus

@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundException(ex: NotFoundException): ResponseEntity<String> {
        return ResponseEntity(ex.message ?: "Resource not found", HttpStatus.NOT_FOUND)
    }

    @ExceptionHandler(CategoryAlreadyExistsException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleCategoryAlreadyExistsException(ex: CategoryAlreadyExistsException): ResponseEntity<String> {
        return ResponseEntity(ex.message ?: "Resource already present", HttpStatus.BAD_REQUEST)
    }
}