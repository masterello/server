package com.masterello.ai.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.masterello.ai.config.AiConfigProperties
import com.masterello.ai.model.AiPrompt
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.MediaType
import org.springframework.stereotype.Service

import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono


@Service
class DefaultAiService(
        @Qualifier("openAiWebClient")
        private val openAIWebClient: WebClient,
        private val aiConfigProperties: AiConfigProperties,
        private val objectMapper: ObjectMapper,
) : AiService {

    override fun <T> process(prompt: AiPrompt, responseType: Class<T>): Mono<T> {
        val request = ChatRequest(
                model = aiConfigProperties.model,
                messages = listOfNotNull(
                        prompt.systemMessage?.let { Message("system", it) },
                        prompt.userMessage?.let { Message("user", it) }
                ),
                temperature = 0.0
        )

        return openAIWebClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ChatResponse::class.java)
                .flatMap { response ->
                    val content = response.choices.firstOrNull()?.message?.content
                            ?: return@flatMap Mono.error(RuntimeException("Empty OpenAI response"))
                    try {
                        val result = objectMapper.readValue(content, responseType)
                        Mono.just(result)
                    } catch (e: Exception) {
                        Mono.error(RuntimeException("Failed to parse translation JSON", e))
                    }
                }
    }

    // DTOs
    data class Message(val role: String, val content: String)

    data class ChatRequest(
            val model: String,
            val messages: List<Message>,
            val temperature: Double
    )

    data class ChatResponse(
            val choices: List<Choice>
    ) {
        data class Choice(
                val message: MessageContent
        )

        data class MessageContent(
                val content: String
        )
    }
}