package com.masterello.auth.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "oauth2_token_pair")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenPair {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "authorization_id", nullable = false)
    private Authorization authorization;

    // Access Token Fields
    @Column(name = "access_token_value", nullable = false, columnDefinition = "TEXT")
    private String accessTokenValue;

    @Column(name = "access_token_type", length = 100)
    private String accessTokenType;

    @Column(name = "access_token_expires_at")
    private OffsetDateTime accessTokenExpiresAt;

    @Column(name = "access_token_metadata", columnDefinition = "TEXT")
    private String accessTokenMetadata;

    @Column(name = "access_token_scopes", length = 1000)
    private String accessTokenScopes;

    // Refresh Token Fields
    @Column(name = "refresh_token_value", nullable = false, columnDefinition = "TEXT")
    private String refreshTokenValue;

    @Column(name = "refresh_token_expires_at")
    private OffsetDateTime refreshTokenExpiresAt;

    @Column(name = "refresh_token_metadata", columnDefinition = "TEXT")
    private String refreshTokenMetadata;

    @Column(name = "is_revoked", nullable = false)
    private boolean revoked = false;

    @Column(name = "issued_at", updatable = false)
    private OffsetDateTime issuedAt;
}
