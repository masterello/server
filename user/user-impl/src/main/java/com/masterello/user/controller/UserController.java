package com.masterello.user.controller;

import com.github.fge.jsonpatch.JsonPatch;
import com.masterello.auth.data.AuthZRole;
import com.masterello.user.dto.AddRoleRequest;
import com.masterello.user.dto.SignUpRequest;
import com.masterello.user.dto.UserDTO;
import com.masterello.user.mapper.UserMapper;
import com.masterello.user.service.SignUpService;
import com.masterello.user.service.UserService;

import com.masterello.user.value.MasterelloUser;
import com.masterello.commons.security.validation.AuthZRule;
import com.masterello.commons.security.validation.AuthZRules;
import com.masterello.commons.security.validation.OwnerId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class UserController {

    private final SignUpService signUpService;
    private final UserService userService;
    private final UserMapper userMapper;

    @RequestMapping(value = "/user/signup", method = RequestMethod.POST)
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
    @GetMapping(value = "/user/{uuid}")
    public UserDTO getUserByUuid(@OwnerId @PathVariable("uuid") @Parameter(required = true) UUID userId) {
        MasterelloUser user = userService.retrieveUserByUuid(userId);
        return userMapper.mapUserToDto(user);
    }

    @AuthZRules({
            @AuthZRule(roles = {AuthZRole.USER, AuthZRole.WORKER}, isOwner = true),
            @AuthZRule(roles = {AuthZRole.ADMIN})
    })
    @Operation(method = "updateUser", tags = "user", responses = {@ApiResponse(responseCode = "200", description = "Returns updated user"), @ApiResponse(responseCode = "404", description = "User is not in the system"), @ApiResponse(responseCode = "500", description = "Error(s) while updating user"),})
    @PatchMapping(value = "/user/{uuid}", produces = {MediaType.APPLICATION_JSON_VALUE}, consumes = {"application/json-patch+json"})
    public UserDTO patchUser(@OwnerId @PathVariable("uuid") UUID userId, @RequestBody JsonPatch patch) {
        MasterelloUser user = userService.updateUser(userId, patch);
        return userMapper.mapUserToDto(user);
    }

    @AuthZRules({
            @AuthZRule(roles = {AuthZRole.USER, AuthZRole.WORKER}, isOwner = true),
            @AuthZRule(roles = {AuthZRole.ADMIN})
    })
    @RequestMapping(value = "/user/{uuid}/add-role", method = RequestMethod.POST)
    public UserDTO addRole(@OwnerId @PathVariable("uuid") @Parameter(required = true) UUID userId, @RequestBody AddRoleRequest request) {
        MasterelloUser user = userService.addRole(userId, request.getRole());
        return userMapper.mapUserToDto(user);
    }
}
