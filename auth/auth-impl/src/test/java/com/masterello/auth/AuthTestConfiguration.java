package com.masterello.auth;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {
        "com.masterello.auth",
        "com.masterello.user"
})
@EnableAutoConfiguration
public class AuthTestConfiguration {
}
