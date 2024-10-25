package com.masterello.user.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class SupportRequestDTO {
    @Null
    private UUID uuid;
    @NotBlank
    @Size(max = 255)
    private String title;
    @NotBlank
    @Size(max = 255)
    private String email;
    private String phone;
    @NotBlank
    @Size(max = 500)
    private String message;
    @Null
    private Boolean processed;
    @Null
    private OffsetDateTime creationDate;
}
