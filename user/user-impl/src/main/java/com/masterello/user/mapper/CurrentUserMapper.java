package com.masterello.user.mapper;

import com.masterello.auth.data.AuthData;
import com.masterello.user.dto.CurrentUserDTO;
import com.masterello.user.value.MasterelloUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserMapper {

    public CurrentUserDTO mapUserToDto(MasterelloUser user, AuthData authData) {
        return CurrentUserDTO.builder()
                .uuid(user.getUuid())  // Assuming MasterelloUser has a getUuid() method
                .title(user.getTitle())
                .name(user.getName())
                .lastname(user.getLastname())
                .email(user.getEmail())
                .phone(user.getPhone())
                .country(user.getCountry())
                .city(user.getCity())
                .emailVerified(user.isEmailVerified())
                .roles(user.getRoles())  // Ensure roles are properly mapped (e.g., Set<Role>)
                .status(user.getStatus())
                .currentAuthType(authData.getAuthType())
                .hasPassword(StringUtils.isNotBlank(user.getPassword()))
                .build();
    }
}
