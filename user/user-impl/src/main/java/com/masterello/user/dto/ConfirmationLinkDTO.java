package com.masterello.user.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class ConfirmationLinkDTO {
    private UUID uuid;
    @NotBlank
    private UUID userUuid;
    @NotBlank
    private String token;
    @NotBlank
    private OffsetDateTime expiresAt;
}
