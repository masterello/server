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

import java.util.Optional;

import static com.masterello.auth.config.AuthConstants.M_TOKEN_COOKIE;
import static com.masterello.auth.config.AuthConstants.R_TOKEN_COOKIE;

public class LogoutRevocationEndpointAuthenticationConverter implements AuthenticationConverter {

    @Override
    public Authentication convert(HttpServletRequest request) {
        Authentication clientPrincipal = SecurityContextHolder.getContext().getAuthentication();

        Optional<String> accessTokenOpt = CookieHelper.retrieve(request.getCookies(), M_TOKEN_COOKIE)
                .filter(StringUtils::hasText);

        if (accessTokenOpt.isPresent()) {
            String token = accessTokenOpt.get();
            return new OAuth2TokenRevocationAuthenticationToken(token, clientPrincipal, OAuth2TokenType.ACCESS_TOKEN.getValue());
        }

        Optional<String> refreshTokenOpt = CookieHelper.retrieve(request.getCookies(), R_TOKEN_COOKIE)
                .filter(StringUtils::hasText);

        if (refreshTokenOpt.isPresent()) {
            String token = refreshTokenOpt.get();
            return new OAuth2TokenRevocationAuthenticationToken(token, clientPrincipal, OAuth2TokenType.REFRESH_TOKEN.getValue());
        }

        throw new TokenCookieNotFoundException("Token cookie not found");
    }
}
