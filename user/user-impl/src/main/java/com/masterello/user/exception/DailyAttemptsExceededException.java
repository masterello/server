package com.masterello.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.TOO_MANY_REQUESTS)
public class DailyAttemptsExceededException extends RuntimeException {

    public DailyAttemptsExceededException(String message) {
        super(message);
    }
}
