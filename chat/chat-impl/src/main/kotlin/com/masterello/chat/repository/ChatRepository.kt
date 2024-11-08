package com.masterello.chat.repository

import com.masterello.chat.domain.Chat
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ChatRepository : JpaRepository<Chat, UUID> {
    fun findByWorkerIdAndTaskId(workerId: UUID, taskId: UUID) : Chat?
}