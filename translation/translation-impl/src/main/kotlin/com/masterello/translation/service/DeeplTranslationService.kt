package com.masterello.translation.service

import com.deepl.api.DeepLClient
import com.deepl.api.LanguageCode
import com.deepl.api.TextTranslationOptions
import com.masterello.translation.dto.TranslationLanguage
import com.masterello.translation.dto.TranslationResponseDTO
import org.springframework.stereotype.Service
import java.util.*

@Service
class DeeplTranslationService(val deepLClient: DeepLClient) : TranslationService {
    private val translationOptions: TextTranslationOptions = TextTranslationOptions()
            .setPreserveFormatting(true)

    private val SUPPORTED_LANGUAGES: EnumSet<TranslationLanguage> = EnumSet.of(TranslationLanguage.EN,
            TranslationLanguage.DE, TranslationLanguage.RU, TranslationLanguage.UK)

    override fun translate(text: String, targetLanguage: TranslationLanguage): TranslationResponseDTO {
        val translateResult = deepLClient.translateText(text, null, targetLanguage.targetCode, translationOptions)
        return TranslationResponseDTO(mapLanguage(translateResult.detectedSourceLanguage), translateResult.text)
    }

    override fun translate(text: String, targetLanguage: TranslationLanguage, originLanguage: TranslationLanguage): TranslationResponseDTO {
        val translateResult = deepLClient.translateText(text, originLanguage.sourceCode, targetLanguage.targetCode, translationOptions)
        return TranslationResponseDTO(mapLanguage(translateResult.detectedSourceLanguage), translateResult.text)    }

    fun mapLanguage(languageCode : String) : TranslationLanguage {
        val langCode = LanguageCode.removeRegionalVariant(languageCode).lowercase()
        return SUPPORTED_LANGUAGES.firstOrNull {LanguageCode.removeRegionalVariant(it.sourceCode).equals(langCode) } ?: TranslationLanguage.OTHER
    }
}