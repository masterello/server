package com.masterello.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.masterello.commons.core.json.Patchable;
import com.masterello.user.value.Language;
import com.masterello.user.value.MasterelloUser;
import com.masterello.user.value.Role;
import com.masterello.user.value.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users", schema = "public")
public class MasterelloUserEntity implements MasterelloUser {

    @Id
    @Column(name = "uuid")
    @GeneratedValue
    private UUID uuid;
    @Column(name = "email")
    private String email;

    @Nullable
    @Column(name = "password")
    private String password;

    @Patchable
    @Column(name = "title")
    private String title;

    @Patchable
    @Column(name = "name")
    private String name;

    @Patchable
    @Column(name = "lastname")
    private String lastname;

    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @Patchable
    @Column(name = "phone")
    private String phone;

    @Patchable
    @Column(name = "city")
    private String city;

    @Column(name = "status")
    private UserStatus status;

    @Column(name = "email_verified")
    @Builder.Default
    private boolean emailVerified = false;

    @Patchable
    @ElementCollection(targetClass = Language.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_languages", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "language")
    private List<Language> languages;


    /**
     * username is expected by spring framework to be a unique user identifier
     * in our case it's email
     */
    @Override
    @Transient
    @JsonIgnore
    public String getUsername() {
        return email;
    }

    @Override
    @Transient
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return status != UserStatus.LOCKED;
    }

    @Override
    @Transient
    @JsonIgnore
    public boolean isEnabled() {
        return status == UserStatus.ACTIVE;
    }

    /**
     * Required by UserDetails interface but not needed for us, hardcode true
     */
    @Override
    @Transient
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Required by UserDetails interface but not needed for us, hardcode true
     */
    @Override
    @Transient
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }
}
