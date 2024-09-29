
package com.masterello.user.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidUserUpdateException extends RuntimeException {

    public InvalidUserUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
