package com.masterello.user.dto;

import com.masterello.user.value.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangeStatusRequestDTO {
    @NotNull
    private UserStatus status;
}
