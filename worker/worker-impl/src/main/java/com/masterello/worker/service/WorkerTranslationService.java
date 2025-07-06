package com.masterello.worker.service;

import com.masterello.translation.dto.TranslationLanguage;
import com.masterello.translation.service.TranslationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Service
@Slf4j
public class WorkerTranslationService {

    private static final EnumSet<TranslationLanguage> SUPPORTED_LANGUAGES = EnumSet.of(TranslationLanguage.EN,
            TranslationLanguage.DE, TranslationLanguage.RU, TranslationLanguage.UK);

    private final TranslationService translationService;

    @Async
    public void detectLanguageAndTranslateText(String origin,
                                               Consumer<TranslatedMessages> translationProcessor,
                                               Consumer<Throwable> errorHandler) {
        try {
            List<TranslatedMessage> messages = new ArrayList<>();

            // translate to english
            var english = translationService.translate(origin, TranslationLanguage.EN);
            messages.add(new TranslatedMessage(origin,
                    english.getDetectedLanguage(), true));

            // if original was not in english - add english translation
            if (english.getDetectedLanguage() != TranslationLanguage.EN) {
                messages.add(new TranslatedMessage(english.getText(), TranslationLanguage.EN, false));
            }

            // translate to other languages excluding original and english
            List<TranslatedMessage> otherTranslations = SUPPORTED_LANGUAGES.parallelStream()
                    .filter(l -> l != english.getDetectedLanguage()) // Exclude original lang if one of supported
                    .filter(l -> l != TranslationLanguage.EN)// Exclude EN
                    .map(l -> {
                        var translation = translationService.translate(english.getText(), l, TranslationLanguage.EN);
                        return new TranslatedMessage(translation.getText(), l, false);
                    })
                    .toList();
            messages.addAll(otherTranslations);
            translationProcessor.accept(new TranslatedMessages(messages));
        } catch (Exception ex) {
            errorHandler.accept(ex);
        }
    }

    public record TranslatedMessage(
            String message,
            TranslationLanguage language,
            boolean original

    ) { }

    public record TranslatedMessages(
            List<TranslatedMessage> messages
    ) { }
}
