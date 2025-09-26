package com.masterello.chat.mapper

import com.masterello.chat.domain.MessageRead
import com.masterello.chat.dto.MessageReadByDTO
import org.mapstruct.Mapper
import org.mapstruct.Mapping

@Mapper(componentModel = "spring")
interface MessageReadMapper {

    @Mapping(target = "readerId", source = "id.recipientId")
    @Mapping(target = "readAt", source = "readAt")
    fun toDto(entity: MessageRead): MessageReadByDTO
}
