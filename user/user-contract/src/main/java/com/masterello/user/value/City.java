package com.masterello.user.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum City {
    BERLIN("be"),
    HAMBURG("hh"),
    MUNICH("m"),
    KOELN("ko"),
    FRANKFURT_AM_MAIN("f"),
    DUESSELDORF("d"),
    STUTTGART("s"),
    LEIPZIG("l"),
    DORTMUND("do"),
    BREMEN("hb"),
    ESSEN("e"),
    DRESDEN("dd"),
    NUERNBERG("n"),
    HANNOVER("h"),
    DUISBURG("du"),
    WUPPERTAL("w"),
    BOCHUM("bo"),
    BIELEFELD("bi"),
    BONN("bn"),
    MANNHEIM("ma");

    @JsonValue
    private final String code;

    @JsonCreator
    public static City getCity(String code) {
        return Arrays.stream(City.values())
                .filter(city -> city.getCode().equalsIgnoreCase(code))
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unknown city " + code));
    }
}



