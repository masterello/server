package com.masterello.user.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class SupportRequestDTO {
    @Null
    private UUID uuid;
    @NotBlank
    private String title;
    @NotBlank
    private String email;
    private String phone;
    @NotBlank
    private String message;
    @Null
    private Boolean processed;
    @Null
    private OffsetDateTime creationDate;
}
