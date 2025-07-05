package com.masterello.ai.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.masterello.ai.config.AiConfigProperties
import com.masterello.ai.model.AiPrompt
import com.masterello.ai.model.AiPromptRef
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
                .bodyToMono(CompletionApiResponse::class.java)
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

    /*
    *
    * {
  "prompt": {
    "id": "pmpt_68692d13cf3481909d9f1c24b4b6cfe70a6dbf58e26ed355",
    "version": "4",
    "variables": {
      "user_input": "example user_input"
    }
  }
}
    * */
    override fun <T> processByRef(prompt: AiPromptRef, responseType: Class<T>): Mono<T> {
        val request = ChatRequestByRef(
                model = aiConfigProperties.model,
                prompt = PromptRef(prompt.promptRef, prompt.promptRefVersion, prompt.params),
        )
        return openAIWebClient.post()
                .uri("/responses")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ResponsesApiResponse::class.java)
                .flatMap { response ->
                    val content = response.output
                            .firstOrNull { it.type == "message" }
                            ?.content
                            ?.firstOrNull { it.type == "output_text" }
                            ?.text
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

    data class ChatRequestByRef(
            val prompt: PromptRef,
            val model: String,
    )

    data class PromptRef(
            val id: String,
            val version: String?,
            val variables: Map<String, String>?
    )

    data class CompletionApiResponse(
            val choices: List<Choice>
    ) {
        data class Choice(
                val message: MessageContent
        )

        data class MessageContent(
                val content: String
        )
    }

    data class ResponsesApiResponse(
            val output: List<OutputItem>
    ) {
        data class OutputItem(
                val type: String,
                val content: List<ContentItem>?
        )

        data class ContentItem(
                val type: String,
                val text: String? // This is a JSON string that you'll need to parse separately
        )
    }


}