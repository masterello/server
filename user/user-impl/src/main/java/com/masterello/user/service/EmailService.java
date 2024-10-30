package com.masterello.user.service;

import com.masterello.user.config.EmailConfigProperties;
import com.masterello.user.value.MasterelloUser;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.UnsupportedEncodingException;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final EmailConfigProperties emailConfigProperties;
    private static final String CONFIRMATION_LINK = "/api/user/confirmationLink/verifyUserToken?code=";
    private static final String RESET_PASSWORD_LINK = "/reset-password?token=";
    private static final String CONTENT = """
            Dear user,<br>
            Please click the link below to verify your registration:<br>
            <h3><a href="${verifyURL}">VERIFY</a></h3>
            Thank you,<br>
            Masterello team
            """;

    private static final String RESET_PASSWORD_CONTENT = """
            Dear user,<br>
            You requested to reset your password on our website. <br>
            If it was not you, please skip this email. <br>
            Please click the link below to reset your password:<br>
            <h3><a href="${resetURL}">Reset password</a></h3>
            Thank you,<br>
            Masterello team
            """;

    private final JavaMailSender mailSender;

    public void sendResetPasswordLink(@NonNull MasterelloUser user,
                                      @NonNull String resetLink)
            throws MessagingException, UnsupportedEncodingException {
        String text = RESET_PASSWORD_CONTENT
                .replace("${resetURL}", buildPasswordResetRequestUrl(resetLink));
        sendMessage(user, text, emailConfigProperties.getResetPassSubject());
    }

    public void sendConfirmationEmail(@NonNull MasterelloUser user, @NonNull String verificationCode) throws MessagingException, UnsupportedEncodingException {
        String text = CONTENT.replace("${verifyURL}", buildRequestUrl(verificationCode));
        sendMessage(user, text, emailConfigProperties.getRegistrationSubject());
    }

    private String buildRequestUrl(String verificationCode) {
        final String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return baseUrl + CONFIRMATION_LINK + verificationCode;
    }

    private String buildPasswordResetRequestUrl(String resetLinkToken) {
        return emailConfigProperties.getServiceUrl() + RESET_PASSWORD_LINK + resetLinkToken;
    }

    private void sendMessage(@NonNull MasterelloUser user,
                             @NonNull String text,
                             @NonNull String subject)
            throws MessagingException, UnsupportedEncodingException {
        if(!emailConfigProperties.isEnabled()) {
            log.info("Email sending is disabled");
            return;
        }
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(emailConfigProperties.getFrom(), emailConfigProperties.getSender());
        helper.setTo(user.getEmail());
        helper.setSubject(subject);
        helper.setText(text, true);

        mailSender.send(message);
    }
}
