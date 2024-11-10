package com.masterello.user.dto;

import com.masterello.commons.core.validation.validator.Password;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignUpRequest {
    @Email
    @NotEmpty
    private String email;
    @NotEmpty
    @Password
    private String password;
}
