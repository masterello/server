package com.masterello.commons.security.filter;

import com.masterello.auth.service.AuthService;
import com.masterello.commons.security.data.MasterelloAuthentication;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

import static com.masterello.auth.config.AuthConstants.M_TOKEN_COOKIE;

@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {
    public static final AnonymousAuthenticationToken ANONYMOUS_AUTHENTICATION_TOKEN = new AnonymousAuthenticationToken("key", "anonymous",
            AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
    private final RequestMatcher requestMatcher;
    private final AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Cookie cookie = WebUtils.getCookie(request, M_TOKEN_COOKIE);
        boolean shouldBeAuthenticated = requestMatcher.matches(request);

        Authentication auth = getAuthenticationFromCookie(cookie);

        if (auth == null) {
            if (shouldBeAuthenticated) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            auth = ANONYMOUS_AUTHENTICATION_TOKEN;
        }

        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }

    private Authentication getAuthenticationFromCookie(Cookie cookie) {
        if (cookie == null) {
            return null;
        }

        return authService.validateToken(cookie.getValue())
                .map(MasterelloAuthentication::new)
                .orElse(null);
    }
}
