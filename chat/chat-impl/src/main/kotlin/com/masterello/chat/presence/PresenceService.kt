package com.masterello.chat.presence

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet

@Service
class PresenceService {
    private val log = KotlinLogging.logger {}

    data class Presence(
        val userId: UUID,
        val sessionIds: MutableSet<String> = CopyOnWriteArraySet(),
        @Volatile var lastSeen: Instant = Instant.EPOCH
    )

    private val presenceByUser: MutableMap<UUID, Presence> = ConcurrentHashMap()

    fun onConnect(userId: UUID, sessionId: String, at: Instant = Instant.now()) {
        val p = presenceByUser.computeIfAbsent(userId) { Presence(userId) }
        p.sessionIds.add(sessionId)
        p.lastSeen = at
        log.debug { "Presence connect user=$userId session=$sessionId sessions=${p.sessionIds.size}" }
    }

    fun onDisconnect(userId: UUID, sessionId: String, at: Instant = Instant.now()) {
        val p = presenceByUser[userId] ?: return
        p.sessionIds.remove(sessionId)
        p.lastSeen = at
        if (p.sessionIds.isEmpty()) {
            // Keep lastSeen for offline display
            log.debug { "Presence offline user=$userId lastSeen=$at" }
        } else {
            log.debug { "Presence still online user=$userId sessions=${p.sessionIds.size}" }
        }
    }

    fun touch(userId: UUID, sessionId: String, at: Instant = Instant.now()) {
        val p = presenceByUser.computeIfAbsent(userId) { Presence(userId) }
        p.sessionIds.add(sessionId)
        p.lastSeen = at
        log.trace { "Presence touch user=$userId session=$sessionId" }
    }

    fun isOnline(userId: UUID): Boolean = presenceByUser[userId]?.sessionIds?.isNotEmpty() == true

    fun getLastSeen(userId: UUID): Instant? = presenceByUser[userId]?.lastSeen
}
