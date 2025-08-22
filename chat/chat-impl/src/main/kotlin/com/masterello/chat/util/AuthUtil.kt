package com.masterello.chat.util

import com.masterello.auth.data.AuthData
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.messaging.simp.stomp.StompHeaderAccessor

object AuthUtil {
    private val log = KotlinLogging.logger {}
    fun getUser(accessor: StompHeaderAccessor): AuthData? {
        val principal = accessor.user
        return if (principal is org.springframework.security.core.Authentication) {
            val details = principal.details
            if (details is AuthData) details else null
        } else null
    }
}
