package com.masterello.user.controller;

import com.masterello.user.dto.RequestPasswordResetDTO;
import com.masterello.user.dto.ResetPasswordDTO;
import com.masterello.user.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @Operation(method = "requestPasswordReset", tags = "password reset",
            description = "Request password reset", responses = {
            @ApiResponse(responseCode = "201", description = "Password reset link is sent"),
            @ApiResponse(responseCode = "400", description = "Daily rate limit exceeded for user"),
            @ApiResponse(responseCode = "404", description = "User with such email was not found"),
            @ApiResponse(responseCode = "406", description = "User registered using oauth, non need to reset password"),
            @ApiResponse(responseCode = "409", description = "User is not activated"),
            @ApiResponse(responseCode = "500", description = "Error(s) while sending password reset email"),
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(value = "/request", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> requestPasswordReset(@Valid @RequestBody RequestPasswordResetDTO requestPasswordReset)
            throws MessagingException, UnsupportedEncodingException {
        passwordResetService.sentPasswordResetLink(requestPasswordReset.getUserEmail(), requestPasswordReset.getLocale());
        return ResponseEntity.ok().build();
    }

    @Operation(method = "resetUserPassword", tags = "password reset", responses = {
            @ApiResponse(responseCode = "200", description = "Password is reset"),
            @ApiResponse(responseCode = "400", description = "Token expired"),
            @ApiResponse(responseCode = "404", description = "Token or user is not found"),
            @ApiResponse(responseCode = "500", description = "Error(s) while resetting password"),
    })
    @PostMapping(produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordDTO resetPasswordDTO) {
        passwordResetService.resetPassword(resetPasswordDTO.getToken(), resetPasswordDTO.getPassword());
        return ResponseEntity.ok().build();
    }
}
