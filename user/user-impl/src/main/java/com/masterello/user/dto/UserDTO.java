package com.masterello.user.dto;

import com.masterello.user.value.City;
import com.masterello.user.value.Country;
import com.masterello.user.value.Role;
import com.masterello.user.value.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@Builder
public class UserDTO {
    @NotBlank
    private UUID uuid;
    private String title;
    private String name;
    private String lastname;
    @NotBlank
    @Email
    private String email;
    private String phone;
    private Country country;
    private City city;
    private Boolean emailVerified;
    private Set<Role> roles;
    private UserStatus status;
}
