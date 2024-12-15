package com.masterello.user.dto;

import com.masterello.commons.core.validation.validator.Password;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordDTO {
    @NotNull
    @NotEmpty
    private String token;
    @Password
    private String password;
}
