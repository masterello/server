package com.masterello.user.dto;

import com.masterello.auth.data.AuthType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class CurrentUserDTO extends UserDTO{
    private boolean hasPassword;
    private AuthType currentAuthType;
}
