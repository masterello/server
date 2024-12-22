package com.masterello.user.exception;

import com.masterello.user.value.UserStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserStatusCannotBeUpdatedException extends RuntimeException{

    public UserStatusCannotBeUpdatedException(UserStatus status) {
        super("User is already in requested status: " + status);
    }
}
