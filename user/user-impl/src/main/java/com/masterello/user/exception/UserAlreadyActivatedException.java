package com.masterello.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class UserAlreadyActivatedException extends RuntimeException {

    public UserAlreadyActivatedException(String message) {
        super(message);
    }
}
