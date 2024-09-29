package com.masterello.auth.responsehandlers;

import com.masterello.auth.helper.CookieHelper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;

import java.io.IOException;

import static com.masterello.auth.config.AuthConstants.M_TOKEN_COOKIE;

public class Oauth2LogoutSuccessAuthHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    /**
     * Removes token cookie
     */
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        Cookie cookie = CookieHelper.generateExpiredCookie(request, M_TOKEN_COOKIE);
        response.addCookie(cookie);
        response.setStatus(HttpStatus.OK.value());
    }


}
