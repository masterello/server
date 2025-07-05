package com.masterello.worker.domain;

import lombok.Getter;

@Getter
public enum TranslationLanguage {
    DE("German"),  // German
    EN("English"),  // English
    UK("Ukrainian") ,  // Ukrainian
    RU("Russian"),  // Russian
    OTHER("other");

    private final String name;

    TranslationLanguage(String name) {
        this.name = name;
    }
}
