package com.masterello.commons.core.validation.handler;

import com.masterello.commons.core.validation.dto.ValidationErrorsDTO;
import com.masterello.commons.core.validation.mapper.ValidationErrorMapper;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(basePackages = {"com.masterello"})
@RequiredArgsConstructor
public class ValidationExceptionHandler {

    private final ValidationErrorMapper validationErrorMapper;

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ValidationErrorsDTO> handleConstraintViolationException(ConstraintViolationException ex) {
        val response = validationErrorMapper.toValidationErrorsDTO(ex.getConstraintViolations());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ValidationErrorsDTO> handleMethodArgumentValidationException(MethodArgumentNotValidException ex) {
        val response = validationErrorMapper.toValidationErrorsDTO(ex.getBindingResult());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
