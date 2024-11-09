package com.masterello.task

import com.masterello.task.service.ReadOnlyTaskService
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class TaskTestConfig {
    @Bean
    open fun getFileService() : ReadOnlyTaskService = mock(ReadOnlyTaskService::class.java)
}