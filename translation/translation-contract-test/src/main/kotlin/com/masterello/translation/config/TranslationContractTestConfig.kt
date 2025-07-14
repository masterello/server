package com.masterello.translation.config

import com.masterello.translation.service.TranslationService
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class TranslationContractTestConfig {

    @Bean
    fun translationService() : TranslationService {
        return mock(TranslationService::class.java)
    }
}