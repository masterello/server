package com.masterello.translation.dto

data class TranslationResponseDTO(val detectedLanguage : TranslationLanguage, val text : String) {
}