package com.masterello.task

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.context.annotation.ComponentScan

@ComponentScan(
    basePackages = ["com.masterello.task", "com.masterello.worker", "com.masterello.auth", "com.masterello.commons"
    ]
)
@EnableAutoConfiguration
class TaskTestConfiguration {
}