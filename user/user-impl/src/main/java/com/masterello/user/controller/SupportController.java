package com.masterello.user.controller;

import com.masterello.auth.data.AuthZRole;
import com.masterello.commons.security.validation.AuthZRule;
import com.masterello.commons.security.validation.AuthZRules;
import com.masterello.user.dto.SupportRequestDTO;
import com.masterello.user.service.SupportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@Slf4j
@Tag(name = "Contact Us")
@RequiredArgsConstructor
@RequestMapping("/api/support")
public class SupportController {
    private final SupportService supportService;

    @Operation(method = "contact", tags = "Contact Us", responses = {
            @ApiResponse(responseCode = "200", description = "Support is stored"),
            @ApiResponse(responseCode = "500", description = "Error(s) while storing support request"),
    })
    @PostMapping(value = "/contact", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> sendSupportRequest(@Valid @RequestBody SupportRequestDTO request) {
        supportService.storeSupportRequest(request);
        return ResponseEntity.ok().build();
    }

    @AuthZRules({
            @AuthZRule(roles = {AuthZRole.ADMIN})
    })
    @Operation(method = "completeRequest", tags = "Contact Us", responses = {
            @ApiResponse(responseCode = "200", description = "Support is marked as processed"),
            @ApiResponse(responseCode = "500", description = "Error(s) while updating support request"),
    })
    @PostMapping(value = "/completeRequest/{requestUuid}", produces = {MediaType.APPLICATION_JSON_VALUE})
    public ResponseEntity<Void> completeRequest(@PathVariable("requestUuid") UUID requestId) {
        supportService.markRequestProcessed(requestId);
        return ResponseEntity.ok().build();
    }

    @AuthZRules({
            @AuthZRule(roles = {AuthZRole.ADMIN})
    })
    @Operation(method = "getAllRequests", tags = "Contact Us", responses = {
            @ApiResponse(responseCode = "200", description = "Receives all support requests"),
            @ApiResponse(responseCode = "500", description = "Error(s) while receiving support requests"),
    })
    @GetMapping(value = "/getAllRequests", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<SupportRequestDTO> getAllRequests() {
        return supportService.receiveAllSupportRequests();
    }

    @AuthZRules({
            @AuthZRule(roles = {AuthZRole.ADMIN})
    })
    @Operation(method = "getAllUnprocessedRequests", tags = "Contact Us", responses = {
            @ApiResponse(responseCode = "200", description = "Receives all unprocessed support requests"),
            @ApiResponse(responseCode = "500", description = "Error(s) while receiving unprocessed support requests"),
    })
    @GetMapping(value = "/getAllUnprocessedRequests", produces = {MediaType.APPLICATION_JSON_VALUE})
    public List<SupportRequestDTO> getAllUnprocessedRequests() {
        return supportService.receiveAllUnprocessedSupportRequests();
    }
}
