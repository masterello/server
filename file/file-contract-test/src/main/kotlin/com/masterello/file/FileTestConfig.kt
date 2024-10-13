package com.masterello.file

import com.masterello.file.service.ReadOnlyFileService
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class FileTestConfig {
    @Bean
    open fun getFileService() : ReadOnlyFileService = mock(ReadOnlyFileService::class.java)
}