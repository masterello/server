package com.masterello.category.config

import com.masterello.category.service.ReadOnlyCategoryService
import org.mockito.Mockito.mock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class CategoryTestConfig {

    @Bean
    open fun getCategoryService() : ReadOnlyCategoryService = mock(ReadOnlyCategoryService::class.java)
}