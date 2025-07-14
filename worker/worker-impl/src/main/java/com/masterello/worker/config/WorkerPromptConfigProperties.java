package com.masterello.worker.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("masterello.worker.ai.prompts")
@Component
@Data
public class WorkerPromptConfigProperties {
    private PromptReference detectLanguage;
    private PromptReference translate;


    public record PromptReference(String id, String version) {}
}