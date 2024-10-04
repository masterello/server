package com.masterello.commons.security.filter;

import com.masterello.commons.security.config.SuperAdminProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class SuperAdminFilter extends OncePerRequestFilter {

    private final SuperAdminProperties superAdminProperties;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Extract the Basic Auth header
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Basic ")) {
            String[] credentials = extractCredentials(authHeader);

            if (credentials != null && validateCredentials(credentials[0], credentials[1])) {
                // Create the Authentication object and set it in the security context
                UserDetails userDetails = new User(superAdminProperties.getUsername(), superAdminProperties.getPassword(), Collections.emptyList());
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        // Proceed with the filter chain
        filterChain.doFilter(request, response);
    }

    private String[] extractCredentials(String authHeader) {
        // Decode the Base64 encoded credentials (Basic base64encoded(username:password))
        try {
            String base64Credentials = authHeader.substring("Basic ".length());
            String credentials = new String(java.util.Base64.getDecoder().decode(base64Credentials));
            return credentials.split(":", 2);
        } catch (IllegalArgumentException e) {
            return null; // Invalid Base64 or credentials format
        }
    }

    private boolean validateCredentials(String username, String password) {
        return superAdminProperties.getUsername().equals(username) && superAdminProperties.getPassword().equals(password);
    }
}
