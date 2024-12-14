package com.masterello.worker;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = {
        "com.masterello.worker",
        "com.masterello.user",
        "com.masterello.user.domain",
        "com.masterello.auth",
        "com.masterello.category",
        "com.masterello.commons"
})
@EnableAutoConfiguration
public class WorkerTestConfiguration {
}
