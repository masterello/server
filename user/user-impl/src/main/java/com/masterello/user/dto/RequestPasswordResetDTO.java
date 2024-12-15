package com.masterello.user.dto;

import jakarta.validation.constraints.Email;
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
public class RequestPasswordResetDTO {
    @NotNull
    @NotEmpty
    @Email
    private String userEmail;
    @NotNull
    @NotEmpty
    private String locale;
}
