package com.masterello.chat.controller

import com.masterello.chat.dto.ChatDTO
import com.masterello.chat.dto.ChatHistoryDTO
import com.masterello.chat.dto.GetOrCreateGeneralChatDTO
import com.masterello.chat.dto.GetOrCreateTaskChatDTO
import com.masterello.chat.dto.MarkReadRequest
import com.masterello.chat.service.ChatReadService
import com.masterello.chat.service.ChatService
import com.masterello.commons.security.util.AuthContextUtil
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.OffsetDateTime
import java.util.*

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService,
    private val chatReadService: ChatReadService
) {

    @Operation(
        summary = "Get general chat", 
        description = "Creates or retrieves a general chat between current user and specified worker"
    )
    @ApiResponse(responseCode = "200", description = "Returns general chat info")
    @GetMapping("")
    @PreAuthorize("@chatSecurity.canCreateGeneralChat(#userId, #workerId)")
    fun getGeneralChat(
        @RequestParam("userId") userId: UUID,
        @RequestParam("workerId") workerId: UUID
    ): ChatDTO {
        return chatService.getGeneralChat(userId, workerId)
    }

    @PostMapping("")
    @PreAuthorize("@chatSecurity.canCreateGeneralChat(#request.userId, #request.workerId)")
    fun createGeneralChat(@RequestBody request: GetOrCreateGeneralChatDTO): ChatDTO {
        return chatService.createGeneralChatPublic(request.userId, request.workerId)
    }

    @Operation(
        summary = "Get task-specific chat", 
        description = "Creates or retrieves a chat for a specific task between task owner and worker"
    )
    @ApiResponse(responseCode = "200", description = "Returns task-specific chat info")
    @GetMapping("/task")
    @PreAuthorize("@chatSecurity.canCreateTaskChat(#taskId, #workerId)")
    fun getTaskChat(@RequestParam("taskId") taskId: UUID, @RequestParam("workerId") workerId: UUID
    ): ChatDTO {
        return chatService.getTaskChat(taskId, workerId)
    }

    @PostMapping("/task")
    @PreAuthorize("@chatSecurity.canCreateTaskChat(#request.taskId, #request.workerId)")
    fun createTaskChat(@RequestBody request: GetOrCreateTaskChatDTO): ChatDTO {
        return chatService.createTaskChatPublic(request.taskId, request.workerId)
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
        summary = "Get user's chats (infinite scroll)",
        description = "Cursor-based retrieval of chats ordered by lastMessageAt desc"
    )
    @ApiResponse(responseCode = "200", description = "Returns scroll page of user's chats")
    @GetMapping("/my-chats/scroll")
    fun getUserChatsScroll(
        @RequestParam(value = "cursor", required = false) cursor: OffsetDateTime?,
        @RequestParam(value = "limit", defaultValue = "30") limit: Int
    ): com.masterello.chat.dto.ChatScrollDTO {
        return chatService.getUserChatsScroll(cursor, limit)
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

    @PostMapping("{chatId}/read")
    @PreAuthorize("@chatSecurity.canAccessChat(#chatId)")
    fun markRead(
        @PathVariable("chatId") chatId: UUID,
        @RequestBody request: MarkReadRequest
    ): Map<String, Any> {
        val readerId = AuthContextUtil.getAuthenticatedUserId()
        val updated = chatReadService.markRead(chatId, readerId, request)
        return mapOf("updated" to updated)
    }

    @Operation(summary = "Get total unread count for current user")
    @GetMapping("/unread/total")
    fun getTotalUnread(): Map<String, Any> {
        val uid = AuthContextUtil.getAuthenticatedUserId()
        val total = chatReadService.totalUnread(uid)
        return mapOf("total" to total)
    }
}
