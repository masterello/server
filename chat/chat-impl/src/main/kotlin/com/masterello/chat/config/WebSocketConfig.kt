package com.masterello.chat.config

import com.masterello.chat.ws.interceptor.WebSocketAuthInterceptor
import com.masterello.chat.ws.interceptor.WebSocketErrorHandler
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer
import org.springframework.web.socket.server.support.DefaultHandshakeHandler
import java.security.Principal

@ConditionalOnProperty(prefix = "masterello.chat.ws", name = ["enabled"], havingValue = "true", matchIfMissing = true)
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(private val webSocketAuthInterceptor: WebSocketAuthInterceptor,
                      private val webSocketErrorHandler: WebSocketErrorHandler,
                      private val chatWebSocketProperties: ChatWebSocketProperties) : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        // Replace with external Broker when traffic increases
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
                .addInterceptors(LoggingHandshakeInterceptor())
                .setHandshakeHandler(object : DefaultHandshakeHandler() {
                    override fun determineUser(
                        request: org.springframework.http.server.ServerHttpRequest,
                        wsHandler: org.springframework.web.socket.WebSocketHandler,
                        attributes: MutableMap<String, Any>
                    ): Principal? {
                        return SecurityContextHolder.getContext().authentication
                    }
                })
                .setAllowedOriginPatterns(*chatWebSocketProperties.allowedOriginPatterns.toTypedArray())

    }

    // Inbound Channel: Handles messages coming from WebSocket clients.
    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration
                .interceptors(webSocketAuthInterceptor)
                .taskExecutor()
                .corePoolSize(chatWebSocketProperties.inboundThreadPool.coreSize) // Minimum number of threads
                .maxPoolSize(chatWebSocketProperties.inboundThreadPool.maxSize) // Maximum number of threads
                .queueCapacity(chatWebSocketProperties.inboundThreadPool.queueCapacity) // Queue size for pending tasks
                .keepAliveSeconds(chatWebSocketProperties.inboundThreadPool.keepAliveSeconds) // Keep-alive time for idle threads
    }

    // Outbound Channel: Sends messages to WebSocket clients.
    override fun configureClientOutboundChannel(registration: ChannelRegistration) {
        registration
                .taskExecutor()
                .corePoolSize(chatWebSocketProperties.outboundThreadPool.coreSize) // Minimum number of threads
                .maxPoolSize(chatWebSocketProperties.outboundThreadPool.maxSize) // Maximum number of threads
                .queueCapacity(chatWebSocketProperties.outboundThreadPool.queueCapacity) // Queue size for pending tasks
                .keepAliveSeconds(chatWebSocketProperties.outboundThreadPool.keepAliveSeconds) // Keep-alive time for idle threads
    }
}
