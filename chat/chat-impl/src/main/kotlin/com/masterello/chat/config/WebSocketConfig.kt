package com.masterello.chat.config

import com.masterello.chat.ws.interceptor.AuthHandshakeInterceptor
import com.masterello.chat.ws.interceptor.WebSocketAuthInterceptor
import com.masterello.chat.ws.interceptor.WebSocketErrorHandler
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(private val authHandshakeInterceptor: AuthHandshakeInterceptor,
                      private val webSocketAuthInterceptor: WebSocketAuthInterceptor,
                      private val webSocketErrorHandler: WebSocketErrorHandler,
                      private val chatWebSocketProperties: ChatWebSocketProperties) : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/topic") // Simple broker for sending messages to clients
                .setTaskScheduler(getTaskScheduler())
                .setHeartbeatValue(longArrayOf(chatWebSocketProperties.serverHeartbeat, chatWebSocketProperties.clientHeartbeat))
        config.setApplicationDestinationPrefixes("/ws") // Prefix for messages sent to the server
    }

    private fun getTaskScheduler(): TaskScheduler {
        val scheduler = ThreadPoolTaskScheduler()
        scheduler.poolSize = chatWebSocketProperties.taskSchedulerPoolSize
        scheduler.setThreadNamePrefix("WebSocketTaskScheduler-")
        scheduler.initialize()
        return scheduler
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry
                .setErrorHandler(webSocketErrorHandler)
                .addEndpoint("/ws/chat")
                .addInterceptors(authHandshakeInterceptor) // Register the interceptor
                .withSockJS() // WebSocket connection endpoint
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(webSocketAuthInterceptor)
    }
}
