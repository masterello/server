package com.masterello.auth.service;

import com.masterello.auth.customgrants.MasterelloAuthenticationToken;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.keygen.Base64StringKeyGenerator;
import org.springframework.security.crypto.keygen.StringKeyGenerator;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationCode;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.Instant;
import java.util.Base64;

import static com.masterello.auth.customgrants.googlegrant.GoogleAuthCodeAuthenticationConverter.GOOGLE_AUTH_CODE_GRANT_TYPE;

@Service
@RequiredArgsConstructor
public class CustomAuthorizationCodeService {

    private final OAuth2AuthorizationService authorizationService;
    private final RegisteredClientRepository registeredClientRepository;
    private final StringKeyGenerator authorizationCodeGenerator =
            new Base64StringKeyGenerator(Base64.getUrlEncoder().withoutPadding(), 96);

    public String generateAuthorizationCode(MasterelloAuthenticationToken principal) {
        RegisteredClient registeredClient = registeredClientRepository.findByClientId("bff");

        OAuth2AuthorizationCode authorizationCode = generateCode(registeredClient);

        OAuth2Authorization authorization = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(principal.getName())
                .authorizationGrantType(new AuthorizationGrantType(GOOGLE_AUTH_CODE_GRANT_TYPE))
                .attribute(Principal.class.getName(), principal)
                .token(authorizationCode)
                .build();

        this.authorizationService.save(authorization);
        return authorizationCode.getTokenValue();
    }

    private OAuth2AuthorizationCode generateCode(RegisteredClient client) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(client.getTokenSettings().getAuthorizationCodeTimeToLive());
        return new OAuth2AuthorizationCode(this.authorizationCodeGenerator.generateKey(), issuedAt, expiresAt);
    }
}
