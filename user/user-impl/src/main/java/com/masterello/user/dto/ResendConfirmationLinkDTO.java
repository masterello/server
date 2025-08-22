package com.masterello.user.dto;

import com.masterello.commons.core.data.Locale;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResendConfirmationLinkDTO {
    @NotNull
    @NotEmpty
    private UUID userUuid;

    private Locale locale;
}
