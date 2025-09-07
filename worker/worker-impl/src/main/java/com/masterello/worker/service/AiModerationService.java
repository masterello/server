package com.masterello.worker.service;

import com.masterello.ai.model.AiPrompt;
import com.masterello.ai.service.AiService;
import com.masterello.worker.dto.ModerationResult;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class AiModerationService implements ModerationService {

    private static final String PROMPT_FILE = "/prompts/moderation.txt";
    private final AiService aiService;
    private String moderationPrompt;

    @PostConstruct
    @SneakyThrows
    public void init() {
        moderationPrompt = readPromptFile(PROMPT_FILE);
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
    public ModerationResult moderateUserInput(String content) {
        return aiService.process(new AiPrompt(moderationPrompt, content), ModerationResult.class).block();
    }
}

