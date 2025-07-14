package com.masterello.translation.service

import com.masterello.translation.dto.TranslationLanguage
import com.masterello.translation.dto.TranslationResponseDTO

interface TranslationService {

    fun translate(text: String, targetLanguage: TranslationLanguage): TranslationResponseDTO

    fun translate(text: String, targetLanguage: TranslationLanguage, originLanguage: TranslationLanguage): TranslationResponseDTO

}