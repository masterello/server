package com.masterello.user.value;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Arrays;

public enum Role {
    USER,
    WORKER,
    ADMIN;

    @JsonCreator
    public Role getRole(String name) {
        return Arrays.stream(Role.values())
                .filter(role -> role.name().equalsIgnoreCase(name))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unknown role " + name ));
    }
}
