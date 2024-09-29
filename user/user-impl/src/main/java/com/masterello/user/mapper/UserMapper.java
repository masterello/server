package com.masterello.user.mapper;

import com.masterello.user.dto.UserDTO;
import com.masterello.user.value.MasterelloUser;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserDTO mapUserToDto(MasterelloUser user);
}
