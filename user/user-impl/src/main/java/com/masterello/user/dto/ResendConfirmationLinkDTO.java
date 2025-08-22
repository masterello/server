package com.masterello.user.dto;

import com.masterello.commons.core.data.Locale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResendConfirmationLinkDTO {
    private Locale locale;
}
