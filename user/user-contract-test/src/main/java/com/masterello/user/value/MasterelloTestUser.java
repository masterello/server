package com.masterello.user.value;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MasterelloTestUser implements MasterelloUser {

    private UUID uuid;
    private String email;
    private String password;
    private String title;
    private String name;
    private String lastname;
    private Set<Role> roles;
    private String phone;
    private Country country;
    private City city;
    private UserStatus status;
    private boolean emailVerified;
    private List<Language> languages;

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED;
    }

    @Override
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
