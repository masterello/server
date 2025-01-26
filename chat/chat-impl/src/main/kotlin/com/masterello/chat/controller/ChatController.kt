package com.masterello.chat.controller

import com.masterello.chat.dto.ChatDTO
import com.masterello.chat.dto.ChatHistoryDTO
import com.masterello.chat.dto.GetOrCreateChatDTO
import com.masterello.chat.service.ChatService
import com.masterello.user.service.MasterelloUserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.OffsetDateTime
import java.util.UUID

@RestController
@RequestMapping("/api/chat")
class ChatController(
        private val service: ChatService,
        private val userService: MasterelloUserService,
) {

    @Operation(summary = "Get Or Create chat", description = "get chat if exists or create otherwise")
    @ApiResponse(responseCode = "200", description = "Returns chat info")
    @PostMapping("")
    fun getOrCreate(@RequestBody request: GetOrCreateChatDTO): ChatDTO {
        return service.getOrCreateChat(request.workerId, request.taskId)
    }

    @Operation(summary = "Get chat history", description = "get chat history")
    @ApiResponse(responseCode = "200", description = "Returns chat history")
    @GetMapping("{chatId}/history")
    fun getChatHistory(@PathVariable("chatId") chatId: UUID,
                       @RequestParam(value = "limit", required = false, defaultValue = "10") limit: Int,
                       @RequestParam(value = "before", required = false, defaultValue = "#{T(java.time.OffsetDateTime).now()}") before: OffsetDateTime
    ): ChatHistoryDTO {
        return service.getChatHistory(chatId, limit, before)
    }
}