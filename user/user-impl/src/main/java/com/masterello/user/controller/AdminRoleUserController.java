package com.masterello.user.controller;

import com.masterello.commons.security.validation.OwnerId;
import com.masterello.user.dto.AddRoleRequest;
import com.masterello.user.dto.UserDTO;
import com.masterello.user.mapper.UserMapper;
import com.masterello.user.service.UserService;
import com.masterello.user.value.MasterelloUser;
import com.masterello.user.value.Role;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user/admin")
public class AdminRoleUserController {

    private final UserService userService;
    private final UserMapper userMapper;

    @RequestMapping(value = "/{uuid}/add-role", method = RequestMethod.POST)
    public UserDTO addAdminRole(@OwnerId @PathVariable("uuid") @Parameter(required = true) UUID userId) {
        MasterelloUser user = userService.addRole(userId, Role.ADMIN);
        return userMapper.mapUserToDto(user);
    }
}
