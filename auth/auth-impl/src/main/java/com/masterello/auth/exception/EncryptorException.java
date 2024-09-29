package com.masterello.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class EncryptorException extends RuntimeException {

    public EncryptorException(String message, Throwable cause) {
        super(message, cause);
    }
}
