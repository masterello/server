package com.masterello.worker.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModerationResult {
    private Decision decision;
    private String[] categories;
    private String explanation;

    @RequiredArgsConstructor
    @Getter
    public enum Decision {

        ALLOW("allow"),
        REVIEW("review"),
        REJECT("reject");

        @JsonValue
        private final String name;

        @JsonCreator
        public static Decision getDecision(String name) {
            return Arrays.stream(Decision.values())
                    .filter(decision -> decision.getName().equalsIgnoreCase(name))
                    .findAny()
                    .orElse(Decision.REJECT);
        }
    }
}
