package com.masterello.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GoogleTokenInfo {
    private String iss;
    private String azp;
    private String aud;
    private String sub;
    private String email;

    @JsonProperty("email_verified")
    private String emailVerified;

    @JsonProperty("at_hash")
    private String atHash;
    private String nonce;
    private String name;
    private String picture;

    @JsonProperty("given_name")
    private String givenName;

    @JsonProperty("family_name")
    private String familyName;
    private String locale;
    private String iat;
    private String exp;
    private String alg;
    private String kid;
    private String typ;
}
