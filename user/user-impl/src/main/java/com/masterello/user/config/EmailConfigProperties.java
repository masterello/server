package com.masterello.user.config;


import com.masterello.commons.core.data.Locale;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@ConfigurationProperties("masterello.user.email")
@Component
@Data
public class EmailConfigProperties {
    private boolean enabled;
    private String from;
    private String sender;
    private Map<Locale, String> registrationSubject;
    private Map<Locale, String> resetPassSubject;
    private Integer resetExpirationMinutes;
    private String serviceUrl;
    private Integer dailyAttempts;
}