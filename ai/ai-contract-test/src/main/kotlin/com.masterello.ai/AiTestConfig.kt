package com.masterello.ai

import com.masterello.ai.service.AiService
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class AiTestConfig {

    @Bean
    open fun getAiService() : AiService = mock(AiService::class.java)
}