package com.masterello.user.exception;

import com.masterello.user.value.Role;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserHasRequestedRoleException extends RuntimeException{

    public UserHasRequestedRoleException(Role role) {
        super("User has requested role: " + role);
    }
}
