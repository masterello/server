package com.masterello.ai.service

import com.masterello.ai.model.AiPrompt
import com.masterello.ai.model.AiPromptRef
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
@ConditionalOnProperty(name = ["masterello.ai.enabled"], havingValue = "none", matchIfMissing = true)
class NoopAiService(
) : AiService {

    override fun <T> process(prompt: AiPrompt, responseType: Class<T>): Mono<T> {
        return Mono.empty()
    }

    override fun <T> processByRef(prompt: AiPromptRef, responseType: Class<T>): Mono<T> {
       return Mono.empty()
    }

}