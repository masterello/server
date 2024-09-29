package com.masterello.auth.revocation;

import com.masterello.auth.exception.TokenCookieNotFoundException;
import com.masterello.auth.helper.CookieHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2TokenRevocationAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.util.StringUtils;

import static com.masterello.auth.config.AuthConstants.M_TOKEN_COOKIE;

public class LogoutRevocationEndpointAuthenticationConverter implements AuthenticationConverter {

    @Override
    public Authentication convert(HttpServletRequest request) {
        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

        String token = CookieHelper.retrieve(request.getCookies(), M_TOKEN_COOKIE)
                .filter(StringUtils::hasText)
                .orElseThrow(() -> new TokenCookieNotFoundException("Token cookie not found"));

        return new OAuth2TokenRevocationAuthenticationToken(token, clientPrincipal, OAuth2TokenType.ACCESS_TOKEN.getValue());
    }
}
