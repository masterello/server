package com.masterello.auth.customgrants.passwordgrant;

import com.masterello.auth.customgrants.AbstractAuthenticationProvider;
import com.masterello.auth.customgrants.MasterelloAuthenticationToken;
import com.masterello.auth.domain.SecurityUserDetails;
import com.masterello.auth.service.SecurityUserDetailsService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Component;

import static com.masterello.auth.customgrants.passwordgrant.CustomPasswordAuthenticationConverter.PASSWORD_GRANT_TYPE;

@Component
public class CustomPasswordAuthenticationProvider extends AbstractAuthenticationProvider {

    private final PasswordEncoder passwordEncoder;

    public CustomPasswordAuthenticationProvider(OAuth2AuthorizationService authorizationService,
                                                OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
                                                SecurityUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        super(authorizationService, tokenGenerator, userDetailsService);
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    protected MasterelloAuthenticationToken getPrincipal(Authentication authentication) {
        CustomPasswordAuthenticationToken customPasswordAuthenticationToken = (CustomPasswordAuthenticationToken) authentication;
        String username = customPasswordAuthenticationToken.getUsername();
        String password = customPasswordAuthenticationToken.getPassword();
        SecurityUserDetails user = fetchUser(username);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.ACCESS_DENIED);
        }

        return new MasterelloAuthenticationToken(user);
    }

    private SecurityUserDetails fetchUser(String username) {
        SecurityUserDetails user;
        try {
            user = userDetailsService.loadUserByUsername(username);
        } catch (UsernameNotFoundException e) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.ACCESS_DENIED);
        }
        return user;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return CustomPasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    protected AuthorizationGrantType getGrantType() {
        return new AuthorizationGrantType(PASSWORD_GRANT_TYPE);
    }
}
