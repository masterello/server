package com.masterello.worker;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@ComponentScan(basePackages = {
        "com.masterello.worker",
        "com.masterello.user",
        "com.masterello.user.domain",
        "com.masterello.auth",
        "com.masterello.category",
        "com.masterello.commons"
})
@EnableAutoConfiguration
@EnableJpaAuditing
public class WorkerTestConfiguration {
}
