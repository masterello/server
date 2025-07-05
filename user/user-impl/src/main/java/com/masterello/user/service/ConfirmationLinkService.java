package com.masterello.user.service;


import com.masterello.commons.async.MasterelloEventPublisher;
import com.masterello.commons.core.data.Locale;
import com.masterello.user.config.EmailConfigProperties;
import com.masterello.user.domain.ConfirmationLink;
import com.masterello.user.domain.MasterelloUserEntity;
import com.masterello.user.dto.ResendConfirmationLinkDTO;
import com.masterello.user.dto.VerifyUserTokenDTO;
import com.masterello.user.event.EmailVerifiedChangedEvent;
import com.masterello.user.exception.ConfirmationLinkNotFoundException;
import com.masterello.user.exception.DailyAttemptsExceededException;
import com.masterello.user.exception.TokenExpiredException;
import com.masterello.user.exception.UserAlreadyActivatedException;
import com.masterello.user.exception.UserNotFoundException;
import com.masterello.user.repository.ConfirmationLinkRepository;
import com.masterello.user.repository.UserRepository;
import com.masterello.user.value.MasterelloUser;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConfirmationLinkService {

    private final EmailService emailService;
    private final EmailConfigProperties emailConfigProperties;
    private final ConfirmationLinkRepository confirmationLinkRepository;
    private final UserRepository userRepository;
    private final MasterelloEventPublisher publisher;

    @Transactional
    public void activateUser(VerifyUserTokenDTO userTokenDTO) {
        var token = userTokenDTO.getToken();
        log.info("Activating user by token {}", token);

        var confirmationLink = confirmationLinkRepository.findByToken(token).orElseThrow(() -> {
            log.info("Confirmation link by token {} is not found", token);
            return new ConfirmationLinkNotFoundException("Confirmation link is not found");
        });

        var user = userRepository.findById(confirmationLink.getUserUuid())
                .orElseThrow(() -> new UserNotFoundException("user is not found"));

        if (user.isEmailVerified()) {
            log.info("user with uuid {} is already verified, skipping", user.getUuid());
            throw new UserAlreadyActivatedException("user is already verified, skipping");
        }

        checkToken(confirmationLink, user);
    }

    public void sendConfirmationLinkSafe(@NonNull MasterelloUser user, @Nullable Locale locale) {
        try {
            sendConfirmationLink(user, locale);
        } catch (MessagingException | IOException e) {
            log.error("Error sending email", e);
        }
    }

    public void sendConfirmationLink(@NonNull MasterelloUser user, @Nullable Locale locale) throws MessagingException, IOException {
        log.info("Sending confirmation link for user {}", user.getEmail());
        String verificationCode = createNewConfirmationToken(user);
        emailService.sendConfirmationEmail(user, verificationCode, locale);
    }

    public void resendConfirmationLink(ResendConfirmationLinkDTO confirmationLinkDTO) throws MessagingException, IOException {
        var user = userRepository.findById(confirmationLinkDTO.getUserUuid())
                .orElseThrow(() -> new UserNotFoundException("user is not found"));

        if (user.isEmailVerified()) {
            log.info("user with uuid {} is already verified, skipping", user.getUuid());
            return;
        }

        var confirmationResendAttempts =
                confirmationLinkRepository.findConfirmationCountsByUserUuid(user.getUuid());

        if(confirmationResendAttempts >= emailConfigProperties.getDailyAttempts()) {
            throw new DailyAttemptsExceededException("Exceeded daily limit for password reset, try again in 24 hours");
        }

        sendConfirmationLink(user, confirmationLinkDTO.getLocale());
    }

    private void checkToken(ConfirmationLink confirmationLink, MasterelloUserEntity user) {
        if (confirmationLink.getExpiresAt().isAfter(OffsetDateTime.now())) {
            log.info("token is valid, switching user to activated state");
            user.setEmailVerified(true);
            val updatedUser = userRepository.saveAndFlush(user);
            publisher.publishEvent(new EmailVerifiedChangedEvent(updatedUser));
        } else {
            log.info("token for user with uuid {} is expired", user.getUuid());
            throw new TokenExpiredException("token is expired");
        }
    }

    private String createNewConfirmationToken(MasterelloUser user) {
        String verificationCode = UUID.randomUUID().toString();

        var confirmationLink = ConfirmationLink.builder()
                .token(verificationCode)
                .userUuid(user.getUuid())
                .expiresAt(OffsetDateTime.now().plusDays(1))
                .creationDate(OffsetDateTime.now())
                .build();

        confirmationLinkRepository.saveAndFlush(confirmationLink);
        return verificationCode;
    }
}
