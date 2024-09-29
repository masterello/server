package com.masterello.auth.helper;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CookieHelperTest {

    @Test
    void retrieve_WithValidCookie_ReturnsValue() {
        // Arrange
        Cookie[] cookies = {new Cookie("testCookie", "testValue")};

        // Act
        Optional<String> result = CookieHelper.retrieve(cookies, "testCookie");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testValue", result.get());
    }

    @Test
    void retrieve_WithInvalidCookie_ReturnsEmptyOptional() {
        // Arrange
        Cookie[] cookies = {new Cookie("testCookie", "testValue")};

        // Act
        Optional<String> result = CookieHelper.retrieve(cookies, "invalidCookie");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void retrieve_WithNullCookies_ReturnsEmptyOptional() {
        // Act
        Optional<String> result = CookieHelper.retrieve(null, "testCookie");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void generateCookie_WithLocalhostDomain_ReturnsCookie() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("localhost");

        // Act
        Cookie result = CookieHelper.generateCookie(request, "testCookie", "testValue", Duration.ofMinutes(30));

        // Assert
        assertNotNull(result);
        assertEquals("testCookie", result.getName());
        assertEquals("testValue", result.getValue());
        assertNull(result.getDomain());
        assertTrue(result.isHttpOnly());
        assertFalse(result.getSecure());
        assertEquals(1800, result.getMaxAge()); // 30 minutes in seconds
        assertEquals("/", result.getPath());
    }

    @Test
    void generateCookie_WithNonLocalhostDomain_ReturnsCookie() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("masterello.com");

        // Act
        Cookie result = CookieHelper.generateCookie(request, "testCookie", "testValue", Duration.ofMinutes(30));

        // Assert
        assertNotNull(result);
        assertEquals("testCookie", result.getName());
        assertEquals("testValue", result.getValue());
        assertEquals("masterello.com", result.getDomain());
        assertTrue(result.isHttpOnly());
        assertFalse(result.getSecure());
        assertEquals(1800, result.getMaxAge()); // 30 minutes in seconds
        assertEquals("/", result.getPath());
    }

    @Test
    void generateExpiredCookie_WithValidInput_ReturnsExpiredCookie() {
        // Arrange
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getServerName()).thenReturn("localhost");

        // Act
        Cookie result = CookieHelper.generateExpiredCookie(request, "expiredCookie");

        // Assert
        assertNotNull(result);
        assertEquals("expiredCookie", result.getName());
        assertEquals("-", result.getValue());
        assertTrue(result.isHttpOnly());
        assertFalse(result.getSecure());
        assertEquals(0, result.getMaxAge()); // Expired cookie
        assertEquals("/", result.getPath());
    }
}