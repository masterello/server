package com.masterello.user.service;


import com.masterello.user.exception.ConfirmationLinkNotFoundException;
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

import java.io.UnsupportedEncodingException;
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
    private ConfirmationLinkRepository confirmationLinkRepository;

    @InjectMocks
    private ConfirmationLinkService confirmationLinkService;

    @Test
    public void activateUser_invalid_link() {
        //GIVEN
        when(confirmationLinkRepository.findByToken(any())).thenReturn(Optional.empty());

        //WHEN
        assertThrows(ConfirmationLinkNotFoundException.class,
                () -> confirmationLinkService.activateUser(UUID.randomUUID().toString()));

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
                () -> confirmationLinkService.activateUser(UUID.randomUUID().toString()));

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
        assertThrows(TokenExpiredException.class, () -> confirmationLinkService.activateUser(UUID.randomUUID().toString()));

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
        assertThrows(UserAlreadyActivatedException.class, () -> confirmationLinkService.activateUser(UUID.randomUUID().toString()));

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
        confirmationLinkService.activateUser(UUID.randomUUID().toString());

        //THEN
        verify(confirmationLinkRepository, times(1)).findByToken(any());
        verify(userRepository, times(1)).findById(any());
        verify(userRepository, times(1)).saveAndFlush(any());
        verifyNoMoreInteractions(confirmationLinkRepository, userRepository);
    }

    @Test
    public void resendConfirmationLink_no_user() {
        //WHEN
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        //WHEN
        assertThrows(UserNotFoundException.class,
                () -> confirmationLinkService.resendConfirmationLink(UUID.randomUUID(), null));

        //THEN
        verify(userRepository, times(1)).findById(any());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void resendConfirmationLink_no_link() throws MessagingException, UnsupportedEncodingException {
        //WHEN
        when(userRepository.findById(any())).thenReturn(Optional.of(buildUser()));
        when(confirmationLinkRepository.findByUserUuid(any())).thenReturn(Optional.empty());
        when(confirmationLinkRepository.saveAndFlush(any())).thenReturn(buildConfirmationLink());
        doNothing().when(emailService).sendConfirmationEmail(any(), any(), isNull());

        //WHEN
        confirmationLinkService.resendConfirmationLink(UUID.randomUUID(), null);

        //THEN
        verify(userRepository, times(1)).findById(any());
        verify(confirmationLinkRepository, times(1)).findByUserUuid(any());
        verify(confirmationLinkRepository, times(1)).saveAndFlush(any());
        verify(emailService, times(1)).sendConfirmationEmail(any(), any(), isNull());
        verifyNoMoreInteractions(userRepository, confirmationLinkRepository, emailService);
    }

    @Test
    public void resendConfirmationLink_expired_token() throws MessagingException, UnsupportedEncodingException {
        //WHEN
        var link = buildConfirmationLink();
        link.setExpiresAt(OffsetDateTime.now().minusDays(1));

        when(userRepository.findById(any())).thenReturn(Optional.of(buildUser()));
        when(confirmationLinkRepository.findByUserUuid(any())).thenReturn(Optional.of(link));
        when(confirmationLinkRepository.saveAndFlush(any())).thenReturn(buildConfirmationLink());
        doNothing().when(emailService).sendConfirmationEmail(any(), any(), isNull());

        //WHEN
        confirmationLinkService.resendConfirmationLink(UUID.randomUUID(), null);

        //THEN
        verify(userRepository, times(1)).findById(any());
        verify(confirmationLinkRepository, times(1)).findByUserUuid(any());
        verify(confirmationLinkRepository, times(1)).saveAndFlush(any());
        verify(emailService, times(1)).sendConfirmationEmail(any(), any(), isNull());
        verifyNoMoreInteractions(userRepository, confirmationLinkRepository, emailService);
    }

    @Test
    public void resendConfirmationLink_already_activated_user() throws MessagingException, UnsupportedEncodingException {
        //WHEN
        var link = buildConfirmationLink();
        link.setExpiresAt(OffsetDateTime.now().minusDays(1));
        var user = buildCompleteUser();
        user.setEmailVerified(true);

        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        //WHEN
        confirmationLinkService.resendConfirmationLink(UUID.randomUUID(), null);

        //THEN
        verify(userRepository, times(1)).findById(any());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    public void resendConfirmationLink() throws MessagingException, UnsupportedEncodingException {
        //WHEN
        when(userRepository.findById(any())).thenReturn(Optional.of(buildUser()));
        when(confirmationLinkRepository.findByUserUuid(any())).thenReturn(Optional.of(buildConfirmationLink()));
        doNothing().when(emailService).sendConfirmationEmail(any(), any(), isNull());

        //WHEN
        confirmationLinkService.resendConfirmationLink(UUID.randomUUID(), null);

        //THEN
        verify(userRepository, times(1)).findById(any());
        verify(confirmationLinkRepository, times(1)).findByUserUuid(any());
        verify(emailService, times(1)).sendConfirmationEmail(any(), any(), isNull());
        verifyNoMoreInteractions(userRepository, confirmationLinkRepository, emailService);
    }

    @Test
    public void sendConfirmationLink_no_token() throws MessagingException, UnsupportedEncodingException {
        //WHEN
        when(confirmationLinkRepository.findByUserUuid(any())).thenReturn(Optional.empty());
        when(confirmationLinkRepository.saveAndFlush(any())).thenReturn(buildConfirmationLink());
        doNothing().when(emailService).sendConfirmationEmail(any(), any(), isNull());

        //WHEN
        confirmationLinkService.sendConfirmationLink(buildUser(), null);

        //THEN
        verify(confirmationLinkRepository, times(1)).findByUserUuid(any());
        verify(confirmationLinkRepository, times(1)).saveAndFlush(any());
        verify(emailService, times(1)).sendConfirmationEmail(any(), any(), isNull());
        verifyNoMoreInteractions(userRepository, confirmationLinkRepository, emailService);
    }

    @Test
    public void sendConfirmationLink_expired_token() throws MessagingException, UnsupportedEncodingException {
        //WHEN
        var link = buildConfirmationLink();
        link.setExpiresAt(OffsetDateTime.now().minusDays(1));

        when(confirmationLinkRepository.findByUserUuid(any())).thenReturn(Optional.of(link));
        when(confirmationLinkRepository.saveAndFlush(any())).thenReturn(buildConfirmationLink());
        doNothing().when(emailService).sendConfirmationEmail(any(), any(), isNull());

        //WHEN
        confirmationLinkService.sendConfirmationLink(buildUser(), null);

        //THEN
        verify(confirmationLinkRepository, times(1)).findByUserUuid(any());
        verify(confirmationLinkRepository, times(1)).saveAndFlush(any());
        verify(emailService, times(1)).sendConfirmationEmail(any(), any(), isNull());
        verifyNoMoreInteractions(userRepository, confirmationLinkRepository, emailService);
    }

    @Test
    public void sendConfirmationLink() throws MessagingException, UnsupportedEncodingException {
        //WHEN
        when(confirmationLinkRepository.findByUserUuid(any())).thenReturn(Optional.of(buildConfirmationLink()));
        doNothing().when(emailService).sendConfirmationEmail(any(), any(), isNull());

        //WHEN
        confirmationLinkService.sendConfirmationLink(buildUser(), null);

        //THEN
        verify(confirmationLinkRepository, times(1)).findByUserUuid(any());
        verify(emailService, times(1)).sendConfirmationEmail(any(), any(), isNull());
        verifyNoMoreInteractions(confirmationLinkRepository, emailService);
    }

    @Test
    public void sendConfirmationLinkSafe_no_token() throws MessagingException, UnsupportedEncodingException {
        //WHEN
        when(confirmationLinkRepository.findByUserUuid(any())).thenReturn(Optional.empty());
        when(confirmationLinkRepository.saveAndFlush(any())).thenReturn(buildConfirmationLink());
        doNothing().when(emailService).sendConfirmationEmail(any(), any(), isNull());

        //WHEN
        confirmationLinkService.sendConfirmationLinkSafe(buildUser(), null);

        //THEN
        verify(confirmationLinkRepository, times(1)).findByUserUuid(any());
        verify(confirmationLinkRepository, times(1)).saveAndFlush(any());
        verify(emailService, times(1)).sendConfirmationEmail(any(), any(), isNull());
        verifyNoMoreInteractions(userRepository, confirmationLinkRepository, emailService);
    }

    @Test
    public void sendConfirmationLinkSafe_expired_token() throws MessagingException, UnsupportedEncodingException {
        //WHEN
        var link = buildConfirmationLink();
        link.setExpiresAt(OffsetDateTime.now().minusDays(1));

        when(confirmationLinkRepository.findByUserUuid(any())).thenReturn(Optional.of(link));
        when(confirmationLinkRepository.saveAndFlush(any())).thenReturn(buildConfirmationLink());
        doNothing().when(emailService).sendConfirmationEmail(any(), any(), isNull());

        //WHEN
        confirmationLinkService.sendConfirmationLinkSafe(buildUser(), null);

        //THEN
        verify(confirmationLinkRepository, times(1)).findByUserUuid(any());
        verify(confirmationLinkRepository, times(1)).saveAndFlush(any());
        verify(emailService, times(1)).sendConfirmationEmail(any(), any(), isNull());
        verifyNoMoreInteractions(userRepository, confirmationLinkRepository, emailService);
    }

    @Test
    public void sendConfirmationLinkSafe() throws MessagingException, UnsupportedEncodingException {
        //WHEN
        when(confirmationLinkRepository.findByUserUuid(any())).thenReturn(Optional.of(buildConfirmationLink()));
        doNothing().when(emailService).sendConfirmationEmail(any(), any(), isNull());

        //WHEN
        confirmationLinkService.sendConfirmationLinkSafe(buildUser(), null);

        //THEN
        verify(confirmationLinkRepository, times(1)).findByUserUuid(any());
        verify(emailService, times(1)).sendConfirmationEmail(any(), any(), isNull());
        verifyNoMoreInteractions(confirmationLinkRepository, emailService);
    }
}
