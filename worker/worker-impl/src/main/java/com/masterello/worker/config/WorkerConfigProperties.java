package com.masterello.worker.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("masterello.worker")
@Component
@Data
public class WorkerConfigProperties {
    private String testWorkerEmailPattern;
}