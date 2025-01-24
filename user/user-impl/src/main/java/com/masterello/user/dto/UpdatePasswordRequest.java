package com.masterello.user.dto;

import com.masterello.commons.core.validation.ErrorCodes;
import com.masterello.commons.core.validation.validator.Password;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePasswordRequest {
    @NotEmpty(message = ErrorCodes.PASSWORD_EMPTY)
    private String oldPassword;
    @NotEmpty(message = ErrorCodes.PASSWORD_EMPTY)
    @Password
    private String newPassword;
}
