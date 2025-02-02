package com.masterello.auth.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Builder
public class AuthData {
    private Boolean emailVerified;
    private List<AuthZRole> userRoles;
    private UUID userId;
    private String username;
    private AuthType authType;

    public List<String> getUserStringRoles() {
        return userRoles == null ? null :
                userRoles.stream()
                        .map(AuthZRole::name)
                        .toList();
    }
}
