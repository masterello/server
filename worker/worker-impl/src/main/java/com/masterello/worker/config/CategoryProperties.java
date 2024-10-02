package com.masterello.worker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("masterello.category")
@Component
@Data
public class CategoryProperties {
    private String categoryServiceUrl;
}
