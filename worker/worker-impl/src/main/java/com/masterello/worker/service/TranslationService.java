package com.masterello.worker.service;

import com.masterello.ai.model.AiPrompt;
import com.masterello.ai.service.AiService;
import com.masterello.worker.domain.TranslationLanguage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
public class TranslationService {

    public static final String DETECT_LANGUAGE_PROMPT_FILE = "/prompts/detect-language.txt";

    public static final String TRANSLATION_PROMPT_FILE = "/prompts/translate-to-language.txt";

    private static final EnumSet<TranslationLanguage> SUPPORTED_LANGUAGES = EnumSet.of(TranslationLanguage.EN,
            TranslationLanguage.DE, TranslationLanguage.RU, TranslationLanguage.UK);

    private final AiService aiService;

    private String detectLanguageTemplate;
    private String translateToLanguageTemplate;

    @PostConstruct
    @SneakyThrows
    public void init() {
        detectLanguageTemplate = readPromptFile(DETECT_LANGUAGE_PROMPT_FILE);
        translateToLanguageTemplate = readPromptFile(TRANSLATION_PROMPT_FILE);
    }

    private String readPromptFile(String fileName) {
        try (InputStream is = getClass().getResourceAsStream(fileName)) {
            if (is == null) {
                throw new RuntimeException("prompt not found in resources");
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load prompt", e);
        }
    }

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
        val systemMessage = detectLanguageTemplate.replace("<USER_INPUT>", origin);
        return aiService.process(new AiPrompt(systemMessage, null), DetectLanguageResponse.class);
    }

    private Mono<TranslatedMessage> translateTask(String origin, TranslationLanguage language) {
        val systemMessage = translateToLanguageTemplate.replace("<USER_INPUT>", origin)
                .replace("<LANGUAGE>", language.getName());
        return aiService.process(new AiPrompt(systemMessage, null), TranslationResponse.class)
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
