package com.masterello.user.controller;

import com.github.fge.jsonpatch.JsonPatch;
import com.masterello.auth.data.AuthData;
import com.masterello.auth.data.AuthZRole;
import com.masterello.commons.core.validation.dto.ValidationErrorsDTO;
import com.masterello.commons.security.data.MasterelloAuthentication;
import com.masterello.user.dto.AddRoleRequest;
import com.masterello.user.dto.SignUpRequest;
import com.masterello.user.dto.UpdatePasswordRequest;
import com.masterello.user.dto.UserDTO;
import com.masterello.user.exception.InvalidUserUpdateException;
import com.masterello.user.mapper.UserMapper;
import com.masterello.user.service.SignUpService;
import com.masterello.user.service.UserService;

import com.masterello.user.value.MasterelloUser;
import com.masterello.commons.security.validation.AuthZRule;
import com.masterello.commons.security.validation.AuthZRules;
import com.masterello.commons.security.validation.OwnerId;
import com.masterello.user.value.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/user")
public class UserController {

    private final SignUpService signUpService;
    private final UserService userService;
    private final UserMapper userMapper;

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO signUp(@RequestBody SignUpRequest request) {
        MasterelloUser user = signUpService.selfSignup(request.getEmail(), request.getPassword());
        return userMapper.mapUserToDto(user);
    }

    @AuthZRules({
            @AuthZRule(roles = {AuthZRole.USER, AuthZRole.WORKER}, isOwner = true),
            @AuthZRule(roles = {AuthZRole.ADMIN})
    })
    @Operation(method = "retrieveUserByUuid", tags = "user", responses = {@ApiResponse(responseCode = "200", description = "Returns user"), @ApiResponse(responseCode = "404", description = "User is not in the system"), @ApiResponse(responseCode = "500", description = "Error(s) while retrieving user"),})
    @GetMapping(value = "/{uuid}")
    public UserDTO getUserByUuid(@OwnerId @PathVariable("uuid") @Parameter(required = true) UUID userId) {
        MasterelloUser user = userService.retrieveUserByUuid(userId);
        return userMapper.mapUserToDto(user);
    }

    @Operation(method = "retrieveCurrentUser", tags = "user", responses = {@ApiResponse(responseCode = "200", description = "Returns user"), @ApiResponse(responseCode = "404", description = "User is not in the system"), @ApiResponse(responseCode = "500", description = "Error(s) while retrieving user"),})
    @GetMapping
    public UserDTO getCurrentUserData() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        AuthData tokenData = ((MasterelloAuthentication) securityContext.getAuthentication()).getDetails();
        MasterelloUser user = userService.retrieveUserByUuid(tokenData.getUserId());
        return userMapper.mapUserToDto(user);
    }
    @AuthZRules({
            @AuthZRule(roles = {AuthZRole.USER, AuthZRole.WORKER}, isOwner = true),
            @AuthZRule(roles = {AuthZRole.ADMIN})
    })
    @Operation(method = "updateUser", tags = "user", responses = {@ApiResponse(responseCode = "200", description = "Returns updated user"), @ApiResponse(responseCode = "404", description = "User is not in the system"), @ApiResponse(responseCode = "500", description = "Error(s) while updating user"),})
    @PatchMapping(value = "/{uuid}", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {"application/json-patch+json"})
    public UserDTO patchUser(@OwnerId @PathVariable("uuid") UUID userId, @RequestBody JsonPatch patch) {
        MasterelloUser user = userService.updateUser(userId, patch);
        return userMapper.mapUserToDto(user);
    }

    @AuthZRules({
            @AuthZRule(roles = {AuthZRole.USER, AuthZRole.WORKER}, isOwner = true),
            @AuthZRule(roles = {AuthZRole.ADMIN})
    })
    @Operation(method = "addRole", tags = "user", responses = {@ApiResponse(responseCode = "200", description = "Roles updated"), @ApiResponse(responseCode = "404", description = "User is not in the system"), @ApiResponse(responseCode = "500", description = "Error(s) while retrieving user and updating"),})
    @RequestMapping(value = "/{uuid}/add-role", method = RequestMethod.POST)
    public UserDTO addRole(@OwnerId @PathVariable("uuid") @Parameter(required = true) UUID userId, @RequestBody AddRoleRequest request) {
        if(request.getRole() == Role.ADMIN) {
            log.warn("Admin role cannot be assigned with this endpoint");
            throw new InvalidUserUpdateException("Admin role is not supported");

        }
        MasterelloUser user = userService.addRole(userId, request.getRole());
        return userMapper.mapUserToDto(user);
    }

    @AuthZRules({
            @AuthZRule(roles = {AuthZRole.USER, AuthZRole.WORKER}, isOwner = true),
            @AuthZRule(roles = {AuthZRole.ADMIN})
    })
    @Operation(method = "updatePassword", tags = "user", responses = {
            @ApiResponse(responseCode = "200", description = "Password successfully updated"),
            @ApiResponse(responseCode = "404", description = "User is not in the system"),
            @ApiResponse(responseCode = "400", description = "Error(s) while validation the request", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ValidationErrorsDTO.class)
            )),
            @ApiResponse(responseCode = "500", description = "Error(s) while retrieving user and updating")})
    @RequestMapping(value = "/{uuid}/password", method = RequestMethod.POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public void updatePassword(@OwnerId @PathVariable("uuid") @Parameter(required = true) UUID userId, @Validated @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(userId, request.getOldPassword(), request.getNewPassword());
    }
}
