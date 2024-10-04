package com.masterello.commons.security.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("masterello.security.admin")
@Component
@Data
public class SuperAdminProperties {
    private String username;
    private String password;
}