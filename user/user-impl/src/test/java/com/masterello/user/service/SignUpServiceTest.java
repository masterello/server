package com.masterello.user.service;


import com.masterello.user.domain.MasterelloUserEntity;
import com.masterello.user.value.MasterelloUser;
import com.masterello.user.value.Role;
import com.masterello.user.value.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignUpServiceTest {

    public static final String EMAIL = "test@example.com";
    public static final String PASSWORD = "password";
    public static final String ENCODED_PASSWORD = "encodedPassword";
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ConfirmationLinkService confirmationLinkService;

    @InjectMocks
    private SignUpService signUpService;

    @Test
    void selfSignup_Success() {
        // given

        MasterelloUserEntity userToSave = MasterelloUserEntity.builder()
                .email(EMAIL)
                .password(ENCODED_PASSWORD)
                .emailVerified(false)
                .roles(Set.of(Role.USER))
                .status(UserStatus.ACTIVE)
                .build();
        MasterelloUser expectedUser = createTestUser(EMAIL, PASSWORD, false);

        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userService.createUser(eq(userToSave))).thenReturn(expectedUser);

        // when
        MasterelloUser actualUser = signUpService.selfSignup(EMAIL, PASSWORD, null);

        // then
        assertNotNull(actualUser);
        assertEquals(EMAIL, actualUser.getEmail());
        assertTrue(actualUser.getRoles().contains(Role.USER));
        assertFalse(actualUser.isEmailVerified());
        assertEquals(UserStatus.ACTIVE, actualUser.getStatus());

        // Verify interactions with mocks
        verify(passwordEncoder).encode(PASSWORD);
        verify(userService).createUser(userToSave);
        verify(confirmationLinkService).sendConfirmationLinkSafe(expectedUser, null);
    }

    @Test
    void googleSignup_Success() {
        // Arrange
        MasterelloUserEntity userToSave = MasterelloUserEntity.builder()
                .email(EMAIL)
                .emailVerified(true)
                .roles(Set.of(Role.USER))
                .status(UserStatus.ACTIVE)
                .build();

        MasterelloUser expectedUser = createTestUser(EMAIL, null, true);

        when(userService.createUser(eq(userToSave))).thenReturn(expectedUser);

        // Act
        MasterelloUser actualUser = signUpService.googleSignup(EMAIL, null, null);

        // Assert
        assertNotNull(actualUser);
        assertEquals(EMAIL, actualUser.getEmail());
        assertTrue(actualUser.isEmailVerified());
        assertTrue(actualUser.getRoles().contains(Role.USER));
        assertEquals(UserStatus.ACTIVE, actualUser.getStatus());
        assertNull(actualUser.getPassword());

        // Verify interactions with mocks
        verify(userService).createUser(userToSave);
    }

    private MasterelloUser createTestUser(String email, String password, boolean emailVerified) {
        return MasterelloUserEntity.builder()
                .email(email)
                .password(password)
                .emailVerified(emailVerified)
                .roles(Set.of(Role.USER))
                .status(UserStatus.ACTIVE)
                .build();
    }
}