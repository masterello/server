package com.masterello.auth.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "clients", schema = "public")
public class MasterelloRegisteredClient {

    @Id
    private String id;
    @Column(name = "client_id")
    private String clientId;
    @Column(name = "client_id_issued_at")
    private Instant clientIdIssuedAt;
    @Column(name = "client_secret")
    private String clientSecret;
    @Column(name = "client_secret_expires_at")
    private Instant clientSecretExpiresAt;
    @Column(name = "client_name")
    private String clientName;
    @Column(name = "client_authentication_methods")
    private String clientAuthenticationMethods;
    @Column(name = "authorization_grant_types")
    private String authorizationGrantTypes;
    @Column(name = "redirect_uris")
    private String redirectUris;
    @Column(name = "post_logout_redirect_uris")
    private String postLogoutRedirectUris;
    @Column(name = "scopes")
    private String scopes;
}
