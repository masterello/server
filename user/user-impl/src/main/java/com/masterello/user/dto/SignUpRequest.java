package com.masterello.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SignUpRequest {
    private String email;
    private String password;
}
