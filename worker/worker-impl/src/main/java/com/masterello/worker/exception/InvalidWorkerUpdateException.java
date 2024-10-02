
package com.masterello.worker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidWorkerUpdateException extends RuntimeException {

    public InvalidWorkerUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}
