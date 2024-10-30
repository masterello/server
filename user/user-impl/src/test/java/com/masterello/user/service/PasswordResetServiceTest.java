package com.masterello.user.service;

import com.masterello.user.config.EmailConfigProperties;
import com.masterello.user.exception.DailyAttemptsExceededException;
import com.masterello.user.exception.PasswordResetNotFoundException;
import com.masterello.user.exception.TokenExpiredException;
import com.masterello.user.exception.UserNotActivatedException;
import com.masterello.user.exception.UserNotFoundException;
import com.masterello.user.repository.PasswordResetRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.util.Optional;

import static com.masterello.user.util.TestDataProvider.VERIFIED_USER;
import static com.masterello.user.util.TestDataProvider.buildCompleteUser;
import static com.masterello.user.util.TestDataProvider.buildPasswordResetEntity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PasswordResetServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private EmailService emailService;

    @Mock
    private EmailConfigProperties emailConfigProperties;

    @Mock
    private PasswordResetRepository passwordResetRepository;

    @InjectMocks
    private PasswordResetService service;

    @Test
    public void sentPasswordResetLink_no_user() {
        //GIVEN
        when(userService.findByEmail(any())).thenReturn(Optional.empty());

        //WHEN
        //THEN
        assertThrows(UserNotFoundException.class,() -> service.sentPasswordResetLink("test@email.com"));
        verify(userService, times(1)).findByEmail("test@email.com");
    }

    @Test
    public void sentPasswordResetLink_user_not_verified() {
        //GIVEN
        var user = buildCompleteUser();
        when(userService.findByEmail(any())).thenReturn(Optional.of(user));

        //WHEN
        //THEN
        assertThrows(UserNotActivatedException.class,() -> service.sentPasswordResetLink("test@email.com"));
        verify(userService, times(1)).findByEmail("test@email.com");
    }

    @Test
    public void sentPasswordResetLink_rate_limit() {
        //GIVEN
        var user = buildCompleteUser();
        user.setEmailVerified(true);
        when(userService.findByEmail(any())).thenReturn(Optional.of(user));

        when(emailConfigProperties.getDailyAttempts()).thenReturn(3);
        when(passwordResetRepository.findResetCountsByUserUuid(any())).thenReturn(3);

        //WHEN
        //THEN
        assertThrows(DailyAttemptsExceededException.class,() ->
                service.sentPasswordResetLink("test@email.com"));
        verify(userService, times(1)).findByEmail("test@email.com");
        verify(emailConfigProperties, times(1)).getDailyAttempts();
        verify(passwordResetRepository, times(1)).findResetCountsByUserUuid(any());
    }

    @Test
    public void sentPasswordResetLink() throws MessagingException, UnsupportedEncodingException {
        //GIVEN
        var user = buildCompleteUser();
        user.setEmailVerified(true);
        when(userService.findByEmail(any())).thenReturn(Optional.of(user));

        when(emailConfigProperties.getDailyAttempts()).thenReturn(3);
        when(passwordResetRepository.findResetCountsByUserUuid(any())).thenReturn(1);

        //WHEN
        service.sentPasswordResetLink("test@email.com");

        //THEN
        verify(userService, times(1)).findByEmail("test@email.com");
        verify(emailConfigProperties, times(1)).getDailyAttempts();
        verify(passwordResetRepository, times(1)).findResetCountsByUserUuid(any());
        verify(passwordResetRepository, times(1)).saveAndFlush(any());
        verify(emailService, times(1)).sendResetPasswordLink(any(), any());
    }


    @Test
    public void checkPasswordResetToken_no_token() {
        //GIVEN
        when(passwordResetRepository.findByToken(any())).thenReturn(Optional.empty());

        //WHEN
        //THEN
        assertThrows(PasswordResetNotFoundException.class,() -> service.checkPasswordResetToken("test"));
        verify(passwordResetRepository, times(1)).findByToken("test");
    }

    @Test
    public void checkPasswordResetToken_token_expired() {
        //GIVEN
        var passwordToken = buildPasswordResetEntity();
        passwordToken.setExpiresAt(OffsetDateTime.now().minusHours(1));

        when(passwordResetRepository.findByToken(any())).thenReturn(Optional.of(passwordToken));

        //WHEN
        //THEN
        assertThrows(TokenExpiredException.class,() -> service.checkPasswordResetToken("test"));
        verify(passwordResetRepository, times(1)).findByToken("test");
    }

    @Test
    public void checkPasswordResetToken() {
        //GIVEN
        var passwordToken = buildPasswordResetEntity();
        when(passwordResetRepository.findByToken(any())).thenReturn(Optional.of(passwordToken));

        //WHEN
        var dto = service.checkPasswordResetToken("test");
        assertEquals(dto.getUserUuid(), VERIFIED_USER.toString());

        verify(passwordResetRepository, times(1)).findByToken("test");
    }

    @Test
    public void resetPassword() {
        //GIVEN
        //WHEN
        service.resetPassword(VERIFIED_USER,"password");

        //THEN
        verify(passwordResetRepository, times(1)).deleteAllByUserUuid(VERIFIED_USER);
        verify(userService, times(1)).resetPassword(VERIFIED_USER,"password");
    }
}
