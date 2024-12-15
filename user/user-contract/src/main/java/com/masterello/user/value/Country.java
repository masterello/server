package com.masterello.user.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum Country {
    GERMANY("DE");

    @JsonValue
    private final String code;

    @JsonCreator
    public static Country getCountry(String code) {
        return Arrays.stream(Country.values())
                .filter(country -> country.getCode().equalsIgnoreCase(code))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unknown country " + code ));
    }
}


