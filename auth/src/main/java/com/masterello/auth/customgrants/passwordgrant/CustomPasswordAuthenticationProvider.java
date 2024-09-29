package com.masterello.auth.customgrants.passwordgrant;

import com.masterello.auth.customgrants.AbstractAuthenticationProvider;
import com.masterello.auth.service.SecurityUserDetailsService;
import com.masterello.user.service.AuthNService;
import com.masterello.user.value.MasterelloUser;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Component;

import static com.masterello.auth.customgrants.passwordgrant.CustomPasswordAuthenticationConverter.PASSWORD_GRANT_TYPE;

@Component
public class CustomPasswordAuthenticationProvider extends AbstractAuthenticationProvider implements AuthenticationProvider {

    private final AuthNService authNService;

    public CustomPasswordAuthenticationProvider(OAuth2AuthorizationService authorizationService,
                                                OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
                                                SecurityUserDetailsService userDetailsService, AuthNService authNService) {
        super(authorizationService, tokenGenerator, userDetailsService);
        this.authNService = authNService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {

        CustomPasswordAuthenticationToken customPasswordAuthenticationToken = (CustomPasswordAuthenticationToken) authentication;
        String username = customPasswordAuthenticationToken.getUsername();
        String password = customPasswordAuthenticationToken.getPassword();
        MasterelloUser user = fetchUser(username);

        if (!authNService.checkPassword(password, user.getPassword())) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.ACCESS_DENIED);
        }

        return authenticate(user, customPasswordAuthenticationToken);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CustomPasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    protected String getGrantType() {
        return PASSWORD_GRANT_TYPE;
    }
}
