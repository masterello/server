package com.masterello.user.config;


import com.masterello.commons.core.validation.dto.ValidationErrorsDTO;
import com.masterello.commons.core.validation.mapper.ValidationErrorMapper;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import static com.masterello.commons.core.validation.dto.ValidationErrorsDTO.ValidationErrorDTO;

@ControllerAdvice(basePackages = {"com.masterello.user"})
@RequiredArgsConstructor
class UserGlobalExceptionHandler {

    private final ValidationErrorMapper validationErrorMapper;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
     public ResponseEntity<ValidationErrorsDTO> handleMethodArgumentValidationException(MethodArgumentNotValidException ex) {
        val response = validationErrorMapper.toValidationErrorsDTO(ex.getBindingResult());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}