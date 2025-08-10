package com.masterello.chat.mapper

import com.masterello.chat.domain.Chat
import com.masterello.chat.dto.ChatDTO
import com.masterello.chat.exceptions.UserNotFoundException
import com.masterello.user.value.MasterelloUser
import org.springframework.stereotype.Component
import java.util.*

@Component
class ChatMapper {
    
    fun toDTO(chat: Chat, chatParticipantsInfo: Map<UUID, MasterelloUser>): ChatDTO {
        // Null safety checks
        val chatId = chat.id ?: throw IllegalStateException("Chat ID cannot be null")
        val createdAt = chat.createdAt ?: throw IllegalStateException("Chat created timestamp cannot be null")
        
        val user = chatParticipantsInfo[chat.userId] 
            ?: throw UserNotFoundException("User ${chat.userId} is not found")
        val worker = chatParticipantsInfo[chat.workerId] 
            ?: throw UserNotFoundException("Worker ${chat.workerId} is not found")

        return ChatDTO(
            id = chatId,
            userId = chat.userId,
            workerId = chat.workerId,
            chatType = chat.chatType,
            taskId = chat.taskId,
            userName = getName(user),
            workerName = getName(worker),
            createdAt = createdAt,
        )
    }

    private fun getName(user: MasterelloUser): String {
        return "${user.name ?: ""} ${user.lastname ?: ""}".trim()
            .ifBlank { user.username }
    }
}
