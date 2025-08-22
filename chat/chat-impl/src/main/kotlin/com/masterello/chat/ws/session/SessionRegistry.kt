package com.masterello.chat.ws.session

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

@Component
class SessionRegistry {
    private val log = KotlinLogging.logger {}

    private val bySession: MutableMap<String, UUID> = ConcurrentHashMap()
    private val sessionsByUser: MutableMap<UUID, MutableSet<String>> = ConcurrentHashMap()

    fun store(sessionId: String, userId: UUID) {
        bySession[sessionId] = userId
        sessionsByUser.computeIfAbsent(userId) { CopyOnWriteArraySet() }.add(sessionId)

        log.debug { "Session upsert sessionId=$sessionId user=$userId"}
    }

    fun remove(sessionId: String): UUID? {
        val userId = bySession.remove(sessionId)
        sessionsByUser[userId]?.remove(sessionId)
        if (sessionsByUser[userId]?.isEmpty() == true) {
            sessionsByUser.remove(userId)
        }
        log.debug { "Session removed sessionId=$sessionId user=${userId} " }
        return userId
    }

    fun anyOnline(userId: UUID): Boolean =
        sessionsByUser[userId]?.isNotEmpty() == true

}
