package com.masterello.user.controller;

import com.masterello.commons.core.validation.validator.Password;
import com.masterello.user.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

@Validated
@Slf4j
@RestController
@Tag(name = "password reset")
@RequestMapping(value = "/api/passwordReset")
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @Operation(method = "resetUserPassword", tags = "password reset", responses = {
            @ApiResponse(responseCode = "201", description = "Password reset link is sent"),
            @ApiResponse(responseCode = "400", description = "Daily rate limit exceeded for user"),
            @ApiResponse(responseCode = "404", description = "User with such email was not found"),
            @ApiResponse(responseCode = "406", description = "User registered using oauth, non need to reset password"),
            @ApiResponse(responseCode = "409", description = "User is not activated"),
            @ApiResponse(responseCode = "500", description = "Error(s) while sending password reset email"),
    })
    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> resetPassword(@RequestParam @Parameter(required = true) String userEmail,
                                              @RequestParam @Parameter(required = true) String locale)
            throws MessagingException, UnsupportedEncodingException {
        passwordResetService.sentPasswordResetLink(userEmail, locale);
        return ResponseEntity.ok().build();
    }

    @Operation(method = "changeUserPassword", tags = "password reset", responses = {
            @ApiResponse(responseCode = "200", description = "Password is reset"),
            @ApiResponse(responseCode = "400", description = "Token expired"),
            @ApiResponse(responseCode = "404", description = "Token or user is not found"),
            @ApiResponse(responseCode = "500", description = "Error(s) while resetting password"),
    })
    @PostMapping(value = "/{token}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> changeUserPassword(@PathVariable(name = "token") String token,
                                                   @RequestParam @Parameter(required = true)
                                                   @Valid @Password String password) {
        passwordResetService.resetPassword(token, password);
        return ResponseEntity.ok().build();
    }
}
