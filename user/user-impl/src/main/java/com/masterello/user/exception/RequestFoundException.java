package com.masterello.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class RequestFoundException extends RuntimeException{

    public RequestFoundException(String message) {
        super(message);
    }
}
