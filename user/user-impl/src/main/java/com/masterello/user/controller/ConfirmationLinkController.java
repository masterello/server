package com.masterello.user.controller;

import com.masterello.user.service.ConfirmationLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

@Slf4j
@RestController
@Tag(name = "confirmation link")
@RequestMapping(value = "/api/user/confirmationLink")
@RequiredArgsConstructor
public class ConfirmationLinkController {

    private final ConfirmationLinkService confirmationLinkService;

    //TODO: change it to post when FE will be ready
    @Operation(method = "verifyUserToken", tags = "confirmation link", responses = {
            @ApiResponse(responseCode = "200", description = "User link is expired, sent new one"),
            @ApiResponse(responseCode = "204", description = "Successfully activated user"),
            @ApiResponse(responseCode = "404", description = "Confirmation link is not found"),
            @ApiResponse(responseCode = "500", description = "Error(s) while verifying token"),
    })
    @GetMapping(value = "/verifyUserToken", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> verifyToken(@RequestParam @Parameter(required = true) String code) {
        confirmationLinkService.activateUser(code);
        return ResponseEntity.ok().build();
    }

    @Operation(method = "resendUserToken", tags = "confirmation link", responses = {
            @ApiResponse(responseCode = "200", description = "Link is sent"),
            @ApiResponse(responseCode = "500", description = "Error(s) while resending confirmation link"),
    })
    @PostMapping(value = "/resendToken", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> resendConfirmationLink(@RequestParam @Parameter(required = true) UUID userUuid,
                                                       @RequestParam(required = false) @Parameter String locale) throws MessagingException, UnsupportedEncodingException {
        confirmationLinkService.resendConfirmationLink(userUuid, locale);
        return ResponseEntity.ok().build();
    }
}
