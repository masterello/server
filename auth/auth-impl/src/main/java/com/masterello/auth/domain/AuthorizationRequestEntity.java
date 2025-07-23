package com.masterello.auth.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizationRequestEntity {

    @Id
    private String state;

    @Lob
    @Column(name = "request_json", columnDefinition = "TEXT")
    private String requestJson;

    @Column(name = "expires_at")
    private Instant expiresAt;
}

