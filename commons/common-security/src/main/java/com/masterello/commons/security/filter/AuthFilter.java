package com.masterello.commons.security.filter;

import com.masterello.auth.service.AuthService;
import com.masterello.commons.security.data.AnonymousMasterelloAuthentication;
import com.masterello.commons.security.data.MasterelloAuthentication;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {
    private static final String M_TOKEN_COOKIE = "m_token";
    private final AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Cookie cookie = WebUtils.getCookie(request, M_TOKEN_COOKIE);
        Authentication auth;
        if(cookie == null ) {
            auth = new AnonymousMasterelloAuthentication();
        } else {
            auth = authService.validateToken(cookie.getValue())
                    .map(MasterelloAuthentication::new)
                    .map(Authentication.class::cast)
                    .orElseGet(AnonymousMasterelloAuthentication::new);
        }

        SecurityContext securityContext = SecurityContextHolder.getContext();
        securityContext.setAuthentication(auth);

        filterChain.doFilter(request, response);
    }
}
