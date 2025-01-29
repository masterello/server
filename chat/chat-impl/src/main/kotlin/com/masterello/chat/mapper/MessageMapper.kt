package com.masterello.chat.mapper

import com.masterello.chat.domain.Message
import com.masterello.chat.dto.ChatMessageDTO
import org.mapstruct.Mapper

@Mapper(componentModel = "spring")  // If using Spring to manage the mapper bean
interface MessageMapper {

    fun toDto(message: Message): ChatMessageDTO

    fun toEntity(dto: ChatMessageDTO): Message
}
