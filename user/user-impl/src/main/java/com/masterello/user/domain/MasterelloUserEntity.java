package com.masterello.user.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.masterello.commons.core.json.Patchable;
import com.masterello.user.value.City;
import com.masterello.user.value.Country;
import com.masterello.user.value.MasterelloUser;
import com.masterello.user.value.Role;
import com.masterello.user.value.UserStatus;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.lang.Nullable;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
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
    @Column(name = "country")
    private Country country;

    @Patchable
    @Column(name = "city")
    private City city;

    @Column(name = "status")
    private UserStatus status;

    @Column(name = "email_verified")
    @Builder.Default
    private boolean emailVerified = false;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

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
