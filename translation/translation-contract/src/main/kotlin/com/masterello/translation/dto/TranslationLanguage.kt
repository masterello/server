package com.masterello.translation.dto

enum class TranslationLanguage(val sourceCode: String, val targetCode: String) {

    EN("en","en-US"),
    DE("de", "de"),
    RU("ru", "ru"),
    UK("uk", "uk"),
    OTHER("other", "other");
}