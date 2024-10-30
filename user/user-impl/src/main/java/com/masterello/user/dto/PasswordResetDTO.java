package com.masterello.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PasswordResetDTO {
    private String userUuid;
}
