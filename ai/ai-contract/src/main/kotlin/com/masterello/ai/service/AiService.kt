package com.masterello.ai.service

import com.masterello.ai.model.AiPrompt
import com.masterello.ai.model.AiPromptRef
import reactor.core.publisher.Mono

interface AiService {

    fun <T> process(prompt: AiPrompt, responseType: Class<T>): Mono<T>

    fun <T> processByRef(prompt: AiPromptRef, responseType: Class<T>): Mono<T>

}