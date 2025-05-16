package com.masterello.user.controller;

import com.masterello.user.dto.ResendConfirmationLinkDTO;
import com.masterello.user.dto.VerifyUserTokenDTO;
import com.masterello.user.service.ConfirmationLinkService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

@Slf4j
@RestController
@Tag(name = "confirmation link")
@RequestMapping(value = "/api/user/confirmationLink")
@RequiredArgsConstructor
public class ConfirmationLinkController {

    private final ConfirmationLinkService confirmationLinkService;

    @Operation(method = "verifyUserToken", tags = "confirmation link", responses = {
            @ApiResponse(responseCode = "200", description = "User link is expired, sent new one"),
            @ApiResponse(responseCode = "204", description = "Successfully activated user"),
            @ApiResponse(responseCode = "404", description = "Confirmation link is not found"),
            @ApiResponse(responseCode = "500", description = "Error(s) while verifying token"),
    })
    @PostMapping(value = "/verifyUserToken", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> verifyToken(@RequestBody VerifyUserTokenDTO userTokenDTO) {
        confirmationLinkService.activateUser(userTokenDTO);
        return ResponseEntity.ok().build();
    }

    @Operation(method = "resendUserToken", tags = "confirmation link", responses = {
            @ApiResponse(responseCode = "200", description = "Link is sent"),
            @ApiResponse(responseCode = "500", description = "Error(s) while resending confirmation link"),
    })
    @PostMapping(value = "/resendToken", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> resendConfirmationLink(@RequestBody ResendConfirmationLinkDTO confirmationLinkDTO)
            throws MessagingException, IOException {
        confirmationLinkService.resendConfirmationLink(confirmationLinkDTO);
        return ResponseEntity.ok().build();
    }
}
