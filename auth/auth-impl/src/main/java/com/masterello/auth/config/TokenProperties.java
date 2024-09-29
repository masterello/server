package com.masterello.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@ConfigurationProperties("masterello.auth.token")
@Component
@Data
public class TokenProperties {

    private Duration accessTokenTtl;
    private Duration refreshTokenTtl;
}
