package com.masterello.user.dto;

import com.masterello.user.value.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddRoleRequest {
    private Role role;
}
