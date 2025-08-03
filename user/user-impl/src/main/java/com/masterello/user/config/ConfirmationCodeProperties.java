package com.masterello.user.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@ConfigurationProperties("masterello.user.email")
@Component
@Data
public class ConfirmationCodeProperties {
    private int maxAttempts = 3;
    private Integer confirmationCodeLength = 6;
    private Duration codeTtl = Duration.ofHours(1);
    private String cleanupCron = "0 0 0 * * *";
    private Duration cleanupThreshold = Duration.ofDays(3);
}