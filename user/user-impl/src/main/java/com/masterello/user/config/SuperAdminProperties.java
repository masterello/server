package com.masterello.user.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("masterello.user.admin")
@Component
@Data
public class SuperAdminProperties {
    private String username;
    private String password;
}