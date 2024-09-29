package com.masterello.auth.helper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.NonNull;

import java.time.Duration;
import java.util.Optional;

import static java.util.Objects.isNull;

public class CookieHelper {

    private static final String COOKIE_DOMAIN = "localhost";
    private static final Boolean HTTP_ONLY = Boolean.TRUE;
    private static final Boolean SECURE = Boolean.FALSE;

    public static Optional<String> retrieve(Cookie[] cookies, @NonNull String name) {
        if (isNull(cookies)) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equalsIgnoreCase(name)) {
                return Optional.ofNullable(cookie.getValue());
            }
        }
        return Optional.empty();
    }

    public static Cookie generateCookie(@NonNull HttpServletRequest request, @NonNull String name, @NonNull String value, @NonNull Duration maxAge) {
        // Build cookie instance
        Cookie cookie = new Cookie(name, value);
        if (!COOKIE_DOMAIN.equals(request.getServerName())) { // https://stackoverflow.com/a/1188145
            cookie.setDomain(request.getServerName());
        }
        cookie.setHttpOnly(HTTP_ONLY);
        cookie.setSecure(SECURE);
        cookie.setMaxAge((int) maxAge.toSeconds());
        cookie.setPath("/");
        return cookie;
    }

    public static Cookie generateExpiredCookie(@NonNull HttpServletRequest request, @NonNull String name) {
        return generateCookie(request, name, "-", Duration.ZERO);
    }

}