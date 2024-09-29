package com.masterello.auth.customgrants.googlegrant;

import com.masterello.auth.domain.SecurityUserDetails;
import com.masterello.auth.dto.GoogleTokenInfo;
import com.masterello.auth.service.GoogleVerificationService;
import com.masterello.auth.service.SecurityUserDetailsService;
import com.masterello.auth.utils.AuthTestDataProvider;
import com.masterello.user.service.AuthNService;
import com.masterello.user.service.MasterelloUserService;
import com.masterello.user.value.MasterelloTestUser;
import com.masterello.user.value.MasterelloUser;

import com.masterello.user.value.Role;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Token;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleOidAuthenticationProviderTest {

    private static final String GOOGLE_TOKEN = "googleToken";
    private static final String USER_EMAIL = "user@gmail.com";
    @Mock
    private OAuth2AuthorizationService authorizationService;
    @Mock
    private OAuth2TokenGenerator<OAuth2Token> tokenGenerator;
    @Mock
    private GoogleVerificationService googleVerificationService;
    @Mock
    private SecurityUserDetailsService userDetailsService;
    @Mock
    private AuthNService authNService;

    @InjectMocks
    private GoogleOidAuthenticationProvider authenticationProvider;


    @Test
    void authenticate_existingUser_successfully() {
        when(googleVerificationService.verify(GOOGLE_TOKEN)).thenReturn(getGoogleTokenValue());
        when(userDetailsService.existsByEmail(USER_EMAIL)).thenReturn(true);
        when(userDetailsService.loadUserByUsername(USER_EMAIL)).thenReturn(getSecurityUserDetails());

        OAuth2Token accessToken = AuthTestDataProvider.getAccessToken();
        OAuth2Token refreshToken = AuthTestDataProvider.getRefreshToken();
        when(tokenGenerator.generate(any()))
                .thenReturn(accessToken, refreshToken);


        OAuth2ClientAuthenticationToken clientToken = AuthTestDataProvider.prepareClientAuthData();

        GoogleOidAuthenticationToken authenticationToken =
                new GoogleOidAuthenticationToken(clientToken,
                        Map.of(OAuth2ParameterNames.TOKEN, GOOGLE_TOKEN));

        // Act
        Authentication result = authenticationProvider.authenticate(authenticationToken);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof OAuth2AccessTokenAuthenticationToken);
        assertEquals(accessToken, ((OAuth2AccessTokenAuthenticationToken) result).getAccessToken());
        assertEquals(refreshToken, ((OAuth2AccessTokenAuthenticationToken) result).getRefreshToken());
    }

    @Test
    void authenticate_newUser_successfully() {
        when(googleVerificationService.verify(GOOGLE_TOKEN)).thenReturn(getGoogleTokenValue());
        when(userDetailsService.existsByEmail(USER_EMAIL)).thenReturn(false);
        when(authNService.googleSignup(USER_EMAIL)).thenReturn(getUser());

        OAuth2Token accessToken = AuthTestDataProvider.getAccessToken();
        OAuth2Token refreshToken = AuthTestDataProvider.getRefreshToken();
        when(tokenGenerator.generate(any()))
                .thenReturn(accessToken, refreshToken);


        OAuth2ClientAuthenticationToken clientToken = AuthTestDataProvider.prepareClientAuthData();

        GoogleOidAuthenticationToken authenticationToken =
                new GoogleOidAuthenticationToken(clientToken,
                        Map.of(OAuth2ParameterNames.TOKEN, GOOGLE_TOKEN));

        // Act
        Authentication result = authenticationProvider.authenticate(authenticationToken);

        // Assert
        assertNotNull(result);
        assertTrue(result instanceof OAuth2AccessTokenAuthenticationToken);
        assertEquals(accessToken, ((OAuth2AccessTokenAuthenticationToken) result).getAccessToken());
        assertEquals(refreshToken, ((OAuth2AccessTokenAuthenticationToken) result).getRefreshToken());
    }

    @Test
    void authenticate_Fails_whenGoogleVerificationFails() {
        when(googleVerificationService.verify(GOOGLE_TOKEN)).thenReturn(Optional.empty());

        GoogleOidAuthenticationToken authenticationToken =
                new GoogleOidAuthenticationToken(mock(Authentication.class),
                        Map.of(OAuth2ParameterNames.TOKEN, GOOGLE_TOKEN));

        // Act
        assertThrows(OAuth2AuthenticationException.class, () -> authenticationProvider.authenticate(authenticationToken));
    }

    private MasterelloUser getUser() {
        return MasterelloTestUser.builder()
                .email(USER_EMAIL)
                .roles(Set.of(Role.USER))
                .build();
    }

    private SecurityUserDetails getSecurityUserDetails(){
        return new SecurityUserDetails(getUser());
    }

    private Optional<GoogleTokenInfo> getGoogleTokenValue() {
        return Optional.of(GoogleTokenInfo.builder()
                .email(USER_EMAIL)
                .build()
        );
    }


    @Test
    void supports_WithGoogleAuthenticationToken_ReturnsTrue() {
        // Arrange
        Class<GoogleOidAuthenticationToken> authenticationClass = GoogleOidAuthenticationToken.class;

        // Act
        boolean result = authenticationProvider.supports(authenticationClass);

        // Assert
        assertTrue(result);
    }

    @Test
    void supports_WithDifferentAuthenticationToken_ReturnsFalse() {
        // Arrange
        Class<UsernamePasswordAuthenticationToken> authenticationClass = UsernamePasswordAuthenticationToken.class;

        // Act
        boolean result = authenticationProvider.supports(authenticationClass);

        // Assert
        assertFalse(result);
    }

}