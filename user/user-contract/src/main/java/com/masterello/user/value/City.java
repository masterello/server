package com.masterello.user.value;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum City {
    BERLIN("BE"),
    HAMBURG("HH"),
    MUNICH("M"),
    KOELN("KO"),
    FRANKFURT_AM_MAIN("F"),
    DUESSELDORF("D"),
    STUTTGART("S"),
    LEIPZIG("L"),
    DORTMUND("DO"),
    BREMEN("HB"),
    ESSEN("E"),
    DRESDEN("DD"),
    NUERNBERG("N"),
    HANNOVER("H"),
    DUISBURG("DU"),
    WUPPERTAL("W"),
    BOCHUM("BO"),
    BIELEFELD("BI"),
    BONN("BN"),
    MANNHEIM("MA");

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



