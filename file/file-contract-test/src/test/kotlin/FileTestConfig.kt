package com.masterello.file.config

import com.masterello.file.service.ReadOnlyFileService
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class FileTestConfig {

    @Bean
    open fun getCategoryService() : ReadOnlyFileService = mock(ReadOnlyFileService::class.java)
}