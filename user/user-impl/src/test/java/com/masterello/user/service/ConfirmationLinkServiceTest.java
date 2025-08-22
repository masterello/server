package com.masterello.user.service;


import com.masterello.commons.async.MasterelloEventPublisher;
import com.masterello.user.config.ConfirmationCodeProperties;
import com.masterello.user.config.EmailConfigProperties;
import com.masterello.user.dto.ResendConfirmationLinkDTO;
import com.masterello.user.dto.VerifyUserTokenDTO;
import com.masterello.user.exception.ConfirmationLinkNotFoundException;
import com.masterello.user.exception.DailyAttemptsExceededException;
import com.masterello.user.exception.TokenExpiredException;
import com.masterello.user.exception.UserAlreadyActivatedException;
import com.masterello.user.exception.UserNotFoundException;
import com.masterello.user.repository.ConfirmationLinkRepository;
import com.masterello.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import static com.masterello.user.util.TestDataProvider.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConfirmationLinkServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailConfigProperties emailConfigProperties;

    @Mock
    private ConfirmationCodeProperties confirmationCodeProperties;
    @Mock
    private ConfirmationLinkRepository confirmationLinkRepository;

    @Mock
    private MasterelloEventPublisher publisher;

    @Mock
    private ConfirmationCodeGenerator codeGenerator;

    @InjectMocks
    private ConfirmationLinkService confirmationLinkService;

    @Test
    public void activateUser_invalid_link() {
        //GIVEN
        when(confirmationLinkRepository.findByToken(any())).thenReturn(Optional.empty());

        //WHEN
        assertThrows(ConfirmationLinkNotFoundException.class,
                () -> confirmationLinkService.activateUser(VerifyUserTokenDTO.builder()
                        .token(UUID.randomUUID().toString())
                        .build()));

        //THEN
        verify(confirmationLinkRepository, times(1)).findByToken(any());
        verifyNoMoreInteractions(confirmationLinkRepository);
    }

    @Test
    public void activateUser_invalid_user() {
        //GIVEN
        when(confirmationLinkRepository.findByToken(any())).thenReturn(Optional.of(buildConfirmationLink()));
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        //WHEN
        assertThrows(UserNotFoundException.class,
                () -> confirmationLinkService.activateUser(VerifyUserTokenDTO.builder()
                        .token(UUID.randomUUID().toString())
                        .build()));

        //THEN
        verify(confirmationLinkRepository, times(1)).findByToken(any());
        verify(userRepository, times(1)).findById(any());
        verifyNoMoreInteractions(confirmationLinkRepository, userRepository);
    }

    @Test
    public void activateUser_expired_token() {
        //GIVEN
        var link = buildConfirmationLink();
        link.setExpiresAt(OffsetDateTime.now().minusDays(1));

        when(confirmationLinkRepository.findByToken(any())).thenReturn(Optional.of(link));
        when(userRepository.findById(any())).thenReturn(Optional.of(buildUser()));

        //WHEN
        assertThrows(TokenExpiredException.class, () -> confirmationLinkService.activateUser(VerifyUserTokenDTO.builder()
                .token(UUID.randomUUID().toString())
                .build()));

        //THEN
        verify(confirmationLinkRepository, times(1)).findByToken(any());
        verify(userRepository, times(1)).findById(any());
        verifyNoMoreInteractions(confirmationLinkRepository, userRepository);
        verifyNoInteractions(emailService);
    }

    @Test
    public void activateUser_already_verified() {
        //GIVEN
        var link = buildConfirmationLink();
        link.setExpiresAt(OffsetDateTime.now().minusDays(1));
        var user = buildCompleteUser();
        user.setEmailVerified(true);

        when(confirmationLinkRepository.findByToken(any())).thenReturn(Optional.of(link));
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        //WHEN
        assertThrows(UserAlreadyActivatedException.class, () -> confirmationLinkService.activateUser(VerifyUserTokenDTO.builder()
                .token(UUID.randomUUID().toString())
                .build()));

        //THEN
        verify(confirmationLinkRepository, times(1)).findByToken(any());
        verify(userRepository, times(1)).findById(any());
        verifyNoMoreInteractions(confirmationLinkRepository, userRepository);
    }

    @Test
    public void activateUser() {
        //GIVEN
        when(confirmationLinkRepository.findByToken(any())).thenReturn(Optional.of(buildConfirmationLink()));
        when(userRepository.findById(any())).thenReturn(Optional.of(buildUser()));
        when(userRepository.saveAndFlush(any())).thenReturn(buildUser());

        //WHEN
        confirmationLinkService.activateUser(VerifyUserTokenDTO.builder()
                .token(UUID.randomUUID().toString())
                .build());

        //THEN
        verify(confirmationLinkRepository, times(1)).findByToken(any());
        verify(userRepository, times(1)).findById(any());
        verify(userRepository, times(1)).saveAndFlush(any());
        verify(publisher).publishEvent(any());
        verifyNoMoreInteractions(confirmationLinkRepository, userRepository);
    }

    @Test
    public void resendConfirmationLink_no_user() {
        //WHEN
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        //WHEN
        assertThrows(UserNotFoundException.class,
                () -> confirmationLinkService.resendConfirmationLink(ResendConfirmationLinkDTO.builder()
                        .userUuid(UUID.randomUUID())
                        .build()));

        //THEN
        verify(userRepository, times(1)).findById(any());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void resendConfirmationLink_already_activated_user() throws MessagingException, IOException {
        //WHEN
        var link = buildConfirmationLink();
        link.setExpiresAt(OffsetDateTime.now().minusDays(1));
        var user = buildCompleteUser();
        user.setEmailVerified(true);

        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        //WHEN
        confirmationLinkService.resendConfirmationLink(ResendConfirmationLinkDTO.builder()
                .userUuid(UUID.randomUUID())
                .build());

        //THEN
        verify(userRepository, times(1)).findById(any());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void resendConfirmationLink_exceedAttempts() {
        //WHEN
        when(userRepository.findById(any())).thenReturn(Optional.of(buildUser()));
        when(emailConfigProperties.getDailyAttempts()).thenReturn(3);
        when(confirmationLinkRepository.findConfirmationCountsByUserUuid(any())).thenReturn(10);

        //WHEN
        assertThrows(DailyAttemptsExceededException.class, () ->
                confirmationLinkService.resendConfirmationLink(ResendConfirmationLinkDTO.builder()
                .userUuid(UUID.randomUUID())
                .build()));

        //THEN
        verify(emailConfigProperties, times(1)).getDailyAttempts();
        verify(confirmationLinkRepository, times(1)).findConfirmationCountsByUserUuid(any());
        verify(userRepository, times(1)).findById(any());
        verifyNoMoreInteractions(userRepository, confirmationLinkRepository, emailService);
    }

    @Test
    public void resendConfirmationLink() throws MessagingException, IOException {
        //WHEN
        when(userRepository.findById(any())).thenReturn(Optional.of(buildUser()));
        doNothing().when(emailService).sendConfirmationEmail(any(), any(), isNull());
        when(emailConfigProperties.getDailyAttempts()).thenReturn(3);
        when(confirmationLinkRepository.findConfirmationCountsByUserUuid(any())).thenReturn(1);
        when(confirmationCodeProperties.getConfirmationCodeLength()).thenReturn(6);
        when(confirmationCodeProperties.getMaxAttempts()).thenReturn(3);
        when(codeGenerator.generateRandomDigitCode(6)).thenReturn("123456");

        //WHEN
        confirmationLinkService.resendConfirmationLink(ResendConfirmationLinkDTO.builder()
                .userUuid(UUID.randomUUID())
                .build());

        //THEN
        verify(emailConfigProperties, times(1)).getDailyAttempts();
        verify(confirmationLinkRepository, times(1)).findConfirmationCountsByUserUuid(any());
        verify(userRepository, times(1)).findById(any());
        verify(confirmationLinkRepository, times(1)).saveAndFlush(any());
        verify(emailService, times(1)).sendConfirmationEmail(any(), any(), isNull());
        verifyNoMoreInteractions(userRepository, confirmationLinkRepository, emailService);
    }

    @Test
    public void sendConfirmationLink() throws MessagingException, IOException {
        //WHEN
        doNothing().when(emailService).sendConfirmationEmail(any(), any(), isNull());
        when(confirmationCodeProperties.getConfirmationCodeLength()).thenReturn(6);
        when(codeGenerator.generateRandomDigitCode(6)).thenReturn("123456");
        when(confirmationCodeProperties.getMaxAttempts()).thenReturn(3);

        //WHEN
        confirmationLinkService.sendConfirmationLink(buildUser(), null);

        //THEN
        verify(emailService, times(1)).sendConfirmationEmail(any(), any(), isNull());
        verify(confirmationLinkRepository, times(1)).saveAndFlush(any());
        verifyNoMoreInteractions(confirmationLinkRepository, emailService);
    }

    @Test
    public void sendConfirmationLinkSafe() throws MessagingException, IOException {
        //WHEN
        doNothing().when(emailService).sendConfirmationEmail(any(), any(), isNull());
        when(confirmationCodeProperties.getConfirmationCodeLength()).thenReturn(6);
        when(codeGenerator.generateRandomDigitCode(6)).thenReturn("123456");
        when(confirmationCodeProperties.getMaxAttempts()).thenReturn(3);

        //WHEN
        confirmationLinkService.sendConfirmationLinkSafe(buildUser(), null);

        //THEN
        verify(confirmationLinkRepository, times(1)).saveAndFlush(any());
        verify(emailService, times(1)).sendConfirmationEmail(any(), any(), isNull());
        verifyNoMoreInteractions(confirmationLinkRepository, emailService);
    }
}
