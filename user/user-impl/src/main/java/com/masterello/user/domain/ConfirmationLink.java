package com.masterello.user.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "confirmation_link", schema = "public")
public class ConfirmationLink implements Serializable {
    @Id
    @Column(name = "uuid")
    @GeneratedValue
    private UUID uuid;

    @Column(name = "user_uuid")
    private UUID userUuid;

    @Column(name = "token")
    private String token;

    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;
}
