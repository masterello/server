package com.masterello.commons.core.data;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor
public enum Locale {
    RU("ru"),
    UK("uk"),
    EN("en"),
    DE("de");

    @JsonValue
    private final String code;

    @JsonCreator
    public static Locale getLocale(String code) {
        return Arrays.stream(Locale.values())
                .filter(country -> country.getCode().equalsIgnoreCase(code))
                .findAny()
                .orElse(Locale.EN);
    }
}
