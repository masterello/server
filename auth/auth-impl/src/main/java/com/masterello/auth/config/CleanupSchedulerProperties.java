package com.masterello.auth.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("masterello.auth.session-cleanup")
@Component
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CleanupSchedulerProperties {

    private int batchSize;
    private String cron;
}
