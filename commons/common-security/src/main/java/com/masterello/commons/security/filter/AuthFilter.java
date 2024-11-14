package com.masterello.commons.security.filter;

import com.masterello.auth.service.AuthService;
import com.masterello.commons.security.data.MasterelloAuthentication;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.WebUtils;

import java.io.IOException;
import java.util.Optional;

import static com.masterello.auth.config.AuthConstants.M_TOKEN_COOKIE;

@RequiredArgsConstructor
public class AuthFilter extends OncePerRequestFilter {
    private final RequestMatcher requestMatcher;
    private final AuthService authService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if(!requestMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }
        Cookie cookie = WebUtils.getCookie(request, M_TOKEN_COOKIE);
        Authentication auth;

        if (cookie == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        } else {
            // Validate token
            Optional<MasterelloAuthentication> validatedAuth = authService.validateToken(cookie.getValue())
                    .map(MasterelloAuthentication::new);

            if (validatedAuth.isPresent()) {
                auth = validatedAuth.get();
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired token");
                return;
            }
        }
        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }
}
