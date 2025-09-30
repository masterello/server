package com.masterello.chat.mapper

import com.masterello.chat.domain.Message
import com.masterello.chat.domain.MessageRead
import com.masterello.chat.dto.ChatMessageDTO
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring",
        uses = [MessageReadMapper::class])  // If using Spring to manage the mapper bean
interface MessageMapper {

    @Mapping(target = "messageReadBy", source = "reads")
    fun toDto(message: Message, reads: List<MessageRead>): ChatMessageDTO

    fun toEntity(dto: ChatMessageDTO): Message
}
