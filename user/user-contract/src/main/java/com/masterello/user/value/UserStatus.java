package com.masterello.user.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum UserStatus {
    ACTIVE(0),
    BANNED(1),
    LOCKED(2);

    private final int code;

    UserStatus(int code) {
        this.code = code;
    }

    @JsonValue
    public int getCode() {
        return code;
    }

    @JsonCreator
    public static UserStatus fromCode(int code) {
        return Arrays.stream(UserStatus.values())
                .filter(status -> status.code == code)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unknown UserStatus " + code ));
    }
}
