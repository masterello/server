package com.masterello.user.mapper;

import com.masterello.user.domain.SupportRequest;
import com.masterello.user.dto.SupportRequestDTO;
import com.masterello.user.dto.UserDTO;
import com.masterello.user.value.MasterelloUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SupportRequestMapper {

    @Mapping(target = "processed", constant = "false")
    SupportRequest mapDtoToEntity(SupportRequestDTO supportRequestDTO);

    SupportRequestDTO mapEntityToDto(SupportRequest supportRequest);
}
