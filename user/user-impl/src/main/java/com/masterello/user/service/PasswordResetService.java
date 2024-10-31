package com.masterello.user.service;

import com.masterello.user.config.EmailConfigProperties;
import com.masterello.user.domain.PasswordReset;
import com.masterello.user.exception.DailyAttemptsExceededException;
import com.masterello.user.exception.OAuthRegistrationException;
import com.masterello.user.exception.PasswordResetNotFoundException;
import com.masterello.user.exception.TokenExpiredException;
import com.masterello.user.exception.UserNotActivatedException;
import com.masterello.user.exception.UserNotFoundException;
import com.masterello.user.repository.PasswordResetRepository;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserService userService;
    private final EmailService emailService;
    private final EmailConfigProperties emailConfigProperties;
    private final PasswordResetRepository passwordResetRepository;

    @Transactional
    public void sentPasswordResetLink(String userEmail, String locale)
            throws MessagingException, UnsupportedEncodingException {
        log.info("resetting password for user");
        var user = userService.findByEmail(userEmail).orElseThrow(
                () -> new UserNotFoundException("user is not found"));

        if(!user.isEmailVerified()) {
            throw new UserNotActivatedException("User is not yet activated, please verify email first");
        }
        if(Objects.isNull(user.getPassword())) {
            throw new OAuthRegistrationException("User was registered using OAuth, no need to reset password");
        }

        var passwordResetAttempts =
                passwordResetRepository.findResetCountsByUserUuid(user.getUuid());

        if(passwordResetAttempts >= emailConfigProperties.getDailyAttempts()) {
            throw new DailyAttemptsExceededException("Exceeded daily limit for password reset, try again in 24 hours");
        }

        PasswordReset passwordReset = PasswordReset.builder()
                .token(UUID.randomUUID().toString())
                .expiresAt(OffsetDateTime.now().plusMinutes(emailConfigProperties.getResetExpirationMinutes()))
                .userUuid(user.getUuid())
                .build();

        passwordResetRepository.saveAndFlush(passwordReset);
        log.info("saved password reset entity for user");
        emailService.sendResetPasswordLink(user, passwordReset.getToken(), locale);
    }

    @Transactional
    public void resetPassword(String token, String password) {
        log.info("checking reset password link");
        var passwordResetLink = passwordResetRepository.findByToken(token).orElseThrow(
                () -> new PasswordResetNotFoundException("password reset link not found"));

        if (OffsetDateTime.now().isAfter(passwordResetLink.getExpiresAt())) {
            throw new TokenExpiredException("Token reset link is expired");
        }
        log.info("reset password link found, removing all reset pass records and reset password");

        passwordResetRepository.deleteAllByUserUuid(passwordResetLink.getUserUuid());
        userService.resetPassword(passwordResetLink.getUserUuid(), password);
    }
}
