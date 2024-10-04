package com.masterello.user.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("masterello.user.email")
@Component
@Data
public class EmailConfigProperties {
    private boolean enabled;
    private String from;
    private String sender;
    private String subject;
}