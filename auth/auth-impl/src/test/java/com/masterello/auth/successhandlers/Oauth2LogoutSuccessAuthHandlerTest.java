package com.masterello.auth.successhandlers;

import com.masterello.auth.responsehandlers.Oauth2LogoutSuccessAuthHandler;
import jakarta.servlet.http.Cookie;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;

import java.time.Duration;

import static com.masterello.auth.config.AuthConstants.M_TOKEN_COOKIE;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Oauth2LogoutSuccessAuthHandlerTest {

    @SneakyThrows
    @Test
    void onAuthenticationSuccess() {
        // Mocks

        Oauth2LogoutSuccessAuthHandler logoutSuccessAuthHandler = new Oauth2LogoutSuccessAuthHandler();

        // Request and response
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Mock authentication
        Authentication authentication = Mockito.mock(Authentication.class);

        // Execute the method
        logoutSuccessAuthHandler.onAuthenticationSuccess(request, response, authentication);

        // Verify that the cookie is added to the response
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);
        Cookie expiredCookie = response.getCookie(M_TOKEN_COOKIE);

        assertEquals("-", expiredCookie.getValue());
        assertEquals(Duration.ZERO.toSeconds(), expiredCookie.getMaxAge());


    }
}
