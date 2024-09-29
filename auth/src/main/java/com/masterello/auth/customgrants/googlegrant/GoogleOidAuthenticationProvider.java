package com.masterello.auth.customgrants.googlegrant;

import com.masterello.auth.customgrants.AbstractAuthenticationProvider;
import com.masterello.auth.dto.GoogleTokenInfo;
import com.masterello.auth.service.GoogleVerificationService;

import com.masterello.auth.service.SecurityUserDetailsService;
import com.masterello.user.service.AuthNService;
import com.masterello.user.value.MasterelloUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Component;

@Component
public class GoogleOidAuthenticationProvider extends AbstractAuthenticationProvider implements AuthenticationProvider {

    private final GoogleVerificationService googleVerificationService;
    private final AuthNService authNService;

    @Autowired
    public GoogleOidAuthenticationProvider(OAuth2AuthorizationService authorizationService,
                                           OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
                                           SecurityUserDetailsService userDetailsService,
                                           GoogleVerificationService googleVerificationService,
                                           AuthNService authNService) {
        super(authorizationService, tokenGenerator, userDetailsService);
        this.googleVerificationService = googleVerificationService;
        this.authNService = authNService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        GoogleOidAuthenticationToken googleAuthToken = (GoogleOidAuthenticationToken) authentication;

        GoogleTokenInfo tokenInfo = googleVerificationService.verify(googleAuthToken.getToken())
                .orElseThrow(() -> new OAuth2AuthenticationException(OAuth2ErrorCodes.ACCESS_DENIED));
        String email = tokenInfo.getEmail();
        MasterelloUser user = fetchOrCreateUser(email);
        return authenticate(user, googleAuthToken);
    }

    private MasterelloUser fetchOrCreateUser(String email) {
        if (userDetailsService.existsByEmail(email)) {
            return userDetailsService.loadUserByUsername(email).toMasterelloUser();
        } else {
            return authNService.googleSignup(email);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return GoogleOidAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    protected String getGrantType() {
        return GoogleOidAuthenticationConverter.GOOGLE_OID_GRANT_TYPE;
    }
}

