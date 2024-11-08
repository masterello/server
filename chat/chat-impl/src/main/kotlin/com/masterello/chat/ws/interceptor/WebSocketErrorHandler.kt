package com.masterello.chat.ws.interceptor

import com.fasterxml.jackson.databind.ObjectMapper
import com.masterello.chat.dto.WebSocketError
import com.masterello.chat.dto.WebSocketErrorCode
import com.masterello.chat.exceptions.ChatNotFoundException
import com.masterello.commons.security.exception.UnauthorisedException
import org.springframework.messaging.Message
import org.springframework.messaging.MessageDeliveryException
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.MessageBuilder
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.socket.messaging.StompSubProtocolErrorHandler

@ControllerAdvice
class WebSocketErrorHandler(private val objectMapper: ObjectMapper) : StompSubProtocolErrorHandler() {
    override fun handleClientMessageProcessingError(clientMessage: Message<ByteArray>?, ex: Throwable): Message<ByteArray>? {
        var exception: Throwable = ex;
        if (exception is MessageDeliveryException) {
            exception = exception.cause ?: exception;
        }

        if (exception is UnauthorisedException) {
            return handleUnauthorizedException(clientMessage, exception);
        }

        if (exception is ChatNotFoundException) {
            return handleChatNotFoundException(clientMessage, exception);
        }

        return handleUnknownException(clientMessage, exception);
    }

    private fun handleUnauthorizedException(clientMessage: Message<ByteArray>?, ex: Throwable): Message<ByteArray> {
        val apiError = WebSocketError(WebSocketErrorCode.UNAUTHORIZED, ex.message);

        return prepareErrorMessage(clientMessage, apiError, WebSocketErrorCode.UNAUTHORIZED);
    }

    private fun handleChatNotFoundException(clientMessage: Message<ByteArray>?, ex: Throwable): Message<ByteArray> {
        val apiError = WebSocketError(WebSocketErrorCode.CHAT_NOT_FOUND, ex.message);

        return prepareErrorMessage(clientMessage, apiError, WebSocketErrorCode.CHAT_NOT_FOUND);
    }

    private fun handleUnknownException(clientMessage: Message<ByteArray>?, ex: Throwable): Message<ByteArray> {
        val apiError = WebSocketError(WebSocketErrorCode.UNKNOWN_ERROR, ex.message);

        return prepareErrorMessage(clientMessage, apiError, WebSocketErrorCode.UNKNOWN_ERROR);
    }

    private fun prepareErrorMessage(
            clientMessage: Message<ByteArray>?,
            apiError: WebSocketError,
            errorCode: WebSocketErrorCode): Message<ByteArray> {
        val message: String = objectMapper.writeValueAsString(apiError);

        val accessor: StompHeaderAccessor = StompHeaderAccessor.create(StompCommand.ERROR);

        setReceiptIdForClient(clientMessage, accessor);
        accessor.message = errorCode.name;
        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(message.encodeToByteArray(), accessor.getMessageHeaders());
    }

    private fun setReceiptIdForClient(clientMessage: Message<ByteArray>?, accessor: StompHeaderAccessor) {
        val clientHeaderAccessor: StompHeaderAccessor?
        if (clientMessage != null) {
            clientHeaderAccessor = MessageHeaderAccessor.getAccessor(clientMessage, StompHeaderAccessor::class.java)
            if (clientHeaderAccessor != null) {
                val receiptId = clientHeaderAccessor.receipt
                if (receiptId != null) {
                    accessor.receiptId = receiptId
                }
            }
        }
    }
}