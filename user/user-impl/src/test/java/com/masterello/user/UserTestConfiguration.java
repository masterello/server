package com.masterello.user;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {
        "com.masterello.user",
        "com.masterello.auth",
        "com.masterello.commons"
})
@EnableAutoConfiguration
public class UserTestConfiguration {
}
