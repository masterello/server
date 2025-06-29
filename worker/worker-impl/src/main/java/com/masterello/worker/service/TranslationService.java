package com.masterello.worker.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterello.ai.model.AiPrompt;
import com.masterello.ai.service.AiService;
import com.masterello.worker.domain.TranslationLanguage;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;
import java.util.function.Consumer;

@RequiredArgsConstructor
@Service
@Slf4j
public class TranslationService {

    private final AiService aiService;
    private final ObjectMapper objectMapper;

    private String systemMessage;
    private String userMessageTemplate;

    @PostConstruct
    @SneakyThrows
    public void init() {
        try (InputStream is = getClass().getResourceAsStream("/prompts/translation-prompts.json")) {
            if (is == null) throw new RuntimeException("translation-prompts.json not found in resources");
            Map<String, String> prompts = objectMapper.readValue(is, new TypeReference<>() {
            });
            systemMessage = prompts.get("system_message");
            userMessageTemplate = prompts.get("user_message");
        }
    }

    public void translateText(String origin,
                              Consumer<TranslationResponse> translationProcessor,
                              Consumer<Throwable> translationErrorHandler) {
        val response = aiService.process(new AiPrompt(systemMessage, prepareUserMessage(origin)), TranslationResponse.class);
        response.subscribe(translationProcessor, translationErrorHandler);
    }

    private String prepareUserMessage(String origin) {
        return userMessageTemplate.replace("<USER_INPUT>", origin);
    }

    public record TranslationResponse(
            TranslationLanguage originalLanguage,
            Map<TranslationLanguage, String> translations
    ) {
    }
}
