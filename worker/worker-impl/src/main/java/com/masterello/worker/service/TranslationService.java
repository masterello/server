package com.masterello.worker.service;

import com.masterello.ai.model.AiPromptRef;
import com.masterello.ai.service.AiService;
import com.masterello.worker.config.WorkerPromptConfigProperties;
import com.masterello.worker.domain.TranslationLanguage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class TranslationService {

    private static final EnumSet<TranslationLanguage> SUPPORTED_LANGUAGES = EnumSet.of(TranslationLanguage.EN,
            TranslationLanguage.DE, TranslationLanguage.RU, TranslationLanguage.UK);
    private static final String USER_INPUT_PARAM = "user_input";
    private static final String LANGUAGE_PARAM = "language";

    private final AiService aiService;
    private final WorkerPromptConfigProperties properties;


    public void detectLanguageAndTranslateText(String origin,
                                               Consumer<TranslatedMessages> translationProcessor,
                                               Consumer<Throwable> errorHandler) {
        detectLanguageTask(origin)
                .flatMap(detectLanguageResponse -> prepareTranslationTasks(origin, detectLanguageResponse))
                .subscribe(
                        translatedMessages -> translationProcessor.accept(new TranslatedMessages(translatedMessages)),
                        errorHandler
                );

    }

    @NotNull
    private Mono<List<TranslatedMessage>> prepareTranslationTasks(String origin,
                                                                  DetectLanguageResponse detectLanguageResponse) {

        List<TranslationLanguage> targetLanguages = SUPPORTED_LANGUAGES.stream()
                .filter(lang -> !lang.equals(detectLanguageResponse.originalLanguage))
                .toList();

        List<Mono<TranslatedMessage>> translationTasks = targetLanguages.stream()
                .map(lang -> translateTask(detectLanguageResponse.originalMessage, lang))
                .collect(Collectors.toCollection(ArrayList::new));

        // Add original message
        translationTasks.add(Mono.just(
                new TranslatedMessage(origin, detectLanguageResponse.originalLanguage, true)));
        return Flux.merge(translationTasks)
                .collectList();

    }

    private Mono<DetectLanguageResponse> detectLanguageTask(String origin) {
        Map<String, String> userInputParam = Map.of(USER_INPUT_PARAM, origin);
        return aiService.processByRef(
                new AiPromptRef(properties.getDetectLanguage().id(), properties.getDetectLanguage().version(), userInputParam),
                DetectLanguageResponse.class);
    }

    private Mono<TranslatedMessage> translateTask(String origin, TranslationLanguage language) {
        Map<String, String> userInputParam = Map.of(USER_INPUT_PARAM, origin, LANGUAGE_PARAM, language.getName());
        return aiService.processByRef(
                        new AiPromptRef(properties.getTranslate().id(), properties.getTranslate().version(), userInputParam),
                        TranslationResponse.class)
                .map(r -> new TranslatedMessage(r.translatedMessage, language, false));
    }

    public record DetectLanguageResponse(

            String originalMessage,
            TranslationLanguage originalLanguage,
            String error

    ) {
    }

    public record TranslationResponse(
            String translatedMessage,
            String error

    ) {
    }

    public record TranslatedMessage(
            String message,
            TranslationLanguage language,

            boolean original

    ) {
    }

    public record TranslatedMessages(
            List<TranslatedMessage> messages
    ) {
    }
}
