package com.masterello.user.service;


import com.masterello.user.domain.ConfirmationLink;
import com.masterello.user.domain.MasterelloUserEntity;
import com.masterello.user.dto.ResendConfirmationLinkDTO;
import com.masterello.user.dto.VerifyUserTokenDTO;
import com.masterello.user.exception.ConfirmationLinkNotFoundException;
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
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConfirmationLinkService {

    private final EmailService emailService;
    private final ConfirmationLinkRepository confirmationLinkRepository;
    private final UserRepository userRepository;

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

    public void sendConfirmationLinkSafe(@NonNull MasterelloUser user, @Nullable String locale) {
        try {
            sendConfirmationLink(user, locale);
        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Error sending email", e);
        }
    }

    public void sendConfirmationLink(@NonNull MasterelloUser user, @Nullable String locale) throws MessagingException, UnsupportedEncodingException {
        log.info("Sending confirmation link for user {}", user);
        String verificationCode = checkConfirmationLinkRecord(user);
        emailService.sendConfirmationEmail(user, verificationCode, locale);
    }

    public void resendConfirmationLink(ResendConfirmationLinkDTO confirmationLinkDTO) throws MessagingException, UnsupportedEncodingException {
        var user = userRepository.findById(confirmationLinkDTO.getUserUuid())
                .orElseThrow(() -> new UserNotFoundException("user is not found"));

        if (user.isEmailVerified()) {
            log.info("user with uuid {} is already verified, skipping", user.getUuid());
            return;
        }

        sendConfirmationLink(user, confirmationLinkDTO.getLocale());
    }

    private void checkToken(ConfirmationLink confirmationLink, MasterelloUserEntity user) {
        if (confirmationLink.getExpiresAt().isAfter(OffsetDateTime.now())) {
            log.info("token is valid, switching user to activated state");
            user.setEmailVerified(true);
            userRepository.saveAndFlush(user);
        } else {
            log.info("token for user with uuid {} is expired", user.getUuid());
            throw new TokenExpiredException("token is expired");
        }
    }

    private String checkConfirmationLinkRecord(MasterelloUser user) {
        var existingLink = confirmationLinkRepository.findByUserUuid(user.getUuid());

        if (existingLink.isPresent()) {
            var confirmationLinkRecord = existingLink.get();

            if (confirmationLinkRecord.getExpiresAt().isBefore(OffsetDateTime.now())) {
                log.info("Current confirmation link is expired, creating new one");
                return refreshConfirmationToken(confirmationLinkRecord);
            } else {
                log.info("Current confirmation link is still active, skipping");
                return confirmationLinkRecord.getToken();
            }
        }
        return createNewConfirmationToken(user);
    }

    private String refreshConfirmationToken(ConfirmationLink confirmationLink) {
        String verificationCode = UUID.randomUUID().toString();

        confirmationLink.setToken(verificationCode);
        confirmationLink.setExpiresAt(OffsetDateTime.now().plusDays(1));

        confirmationLinkRepository.saveAndFlush(confirmationLink);
        return verificationCode;
    }

    private String createNewConfirmationToken(MasterelloUser user) {
        String verificationCode = UUID.randomUUID().toString();

        var confirmationLink = ConfirmationLink.builder()
                .token(verificationCode)
                .userUuid(user.getUuid())
                .expiresAt(OffsetDateTime.now().plusDays(1))
                .build();

        confirmationLinkRepository.saveAndFlush(confirmationLink);
        return verificationCode;
    }
}
