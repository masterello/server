package com.masterello.auth.helper;


import com.masterello.user.value.MasterelloUser;
import com.masterello.user.value.Role;
import lombok.experimental.UtilityClass;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@UtilityClass
public class UserClaimsHelper {

    public Map<String, Object> getUserClaims(MasterelloUser user) {
        Set<String> roles = user.getRoles().stream()
                .map(Role::name).collect(Collectors.toSet());

        return Map.of(
                "roles", roles,
                "userStatus", user.getStatus().name(),
                "username", user.getUsername(),
                "userId", user.getUuid().toString(),
                "emailVerified", user.isEmailVerified()
        );
    }
}
