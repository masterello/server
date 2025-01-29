package com.masterello.chat.mapper

import com.masterello.chat.domain.Chat
import com.masterello.chat.dto.ChatDTO
import com.masterello.chat.exceptions.UserNotFoundException
import com.masterello.user.value.MasterelloUser
import org.springframework.stereotype.Component
import java.util.*

@Component
class ChatMapper {
    fun toDTO(chat: Chat, chatParticipantsInfo: Map<UUID, MasterelloUser>) : ChatDTO {
        val user = chatParticipantsInfo[chat.userId] ?: throw UserNotFoundException("User ${chat.userId} is not found")
        val worker = chatParticipantsInfo[chat.workerId] ?: throw UserNotFoundException("Worker ${chat.workerId} is not found")

        return ChatDTO(
                id = chat.id!!,
                taskId = chat.taskId!!,
                userId = chat.userId!!,
                userName = getName(user),
                workerId = chat.workerId!!,
                workerName = getName(worker),
        )
    }

    private fun getName(user: MasterelloUser) : String {
        return "${user.name ?: ""} ${user.lastname ?: ""}".trim()
                .ifBlank { user.username }
    }

}