package com.masterello.worker;

import com.masterello.worker.service.ReadOnlyWorkerService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
public class WorkerTestConfig {
    @Bean
    public ReadOnlyWorkerService getWorkerService() {
        return mock(ReadOnlyWorkerService.class);
    }
}
