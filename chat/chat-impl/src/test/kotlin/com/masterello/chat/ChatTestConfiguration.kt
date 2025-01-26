package com.masterello.chat

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.ComponentScan

@ComponentScan(basePackages = [
    "com.masterello.chat",
    "com.masterello.user",
    "com.masterello.worker",
    "com.masterello.auth",
    "com.masterello.task",
    "com.masterello.commons"])
@EnableAutoConfiguration
@ConfigurationPropertiesScan
class ChatTestConfiguration {
}