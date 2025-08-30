package com.masterello.chat.controller

import com.masterello.chat.dto.ChatDTO
import com.masterello.chat.dto.ChatHistoryDTO
import com.masterello.chat.dto.GetOrCreateGeneralChatDTO
import com.masterello.chat.dto.GetOrCreateTaskChatDTO
import com.masterello.chat.service.ChatService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.util.*

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService
) {

    @Operation(
        summary = "Get general chat", 
        description = "Creates or retrieves a general chat between current user and specified worker"
    )
    @ApiResponse(responseCode = "200", description = "Returns general chat info")
    @PostMapping("")
    @PreAuthorize("@chatSecurity.canCreateGeneralChat(#request.userId, #request.workerId)")
    fun getOrCreateGeneralChat(@RequestBody request: GetOrCreateGeneralChatDTO): ChatDTO {
        return chatService.getOrCreateGeneralChat(request.userId, request.workerId)
    }

    @Operation(
        summary = "Get task-specific chat", 
        description = "Creates or retrieves a chat for a specific task between task owner and worker"
    )
    @ApiResponse(responseCode = "200", description = "Returns task-specific chat info")
    @PostMapping("/task")
    @PreAuthorize("@chatSecurity.canCreateTaskChat(#request.taskId, #request.workerId)")
    fun getOrCreateTaskChat(@RequestBody request: GetOrCreateTaskChatDTO): ChatDTO {
        return chatService.getOrCreateTaskChat(request.taskId, request.workerId)
    }

    @Operation(
        summary = "Get user's chats", 
        description = "Retrieves all active chats for the current user"
    )
    @ApiResponse(responseCode = "200", description = "Returns list of user's chats")
    @GetMapping("/my-chats")
    fun getUserChats(
        @RequestParam("page", defaultValue = "1") page: Int,
        @RequestParam("size", defaultValue = "30") size: Int
    ): com.masterello.chat.dto.ChatPageDTO {
        return chatService.getUserChats(page, size)
    }

    @Operation(
        summary = "Get chat history", 
        description = "Retrieves message history for a specific chat"
    )
    @ApiResponse(responseCode = "200", description = "Returns chat history")
    @GetMapping("{chatId}/history")
    @PreAuthorize("@chatSecurity.canAccessChat(#chatId)")
    fun getChatHistory(
        @PathVariable("chatId") chatId: UUID,
        @RequestParam(value = "limit", required = false, defaultValue = "10") limit: Int,
        @RequestParam(value = "before", required = false, defaultValue = "#{T(java.time.OffsetDateTime).now()}") before: OffsetDateTime
    ): ChatHistoryDTO {
        return chatService.getChatHistory(chatId, limit, before)
    }
}
