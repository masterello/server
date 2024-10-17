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
    private static final String LINK = "/api/user/confirmationLink/verifyUserToken?code=";
    private static final String CONTENT = """
            Dear user,<br>
            Please click the link below to verify your registration:<br>
            <h3><a href="${verifyURL}">VERIFY</a></h3>
            Thank you,<br>
            Masterello
            """;
    private final JavaMailSender mailSender;

    public void sendEmail(@NonNull MasterelloUser user, @NonNull String verificationCode) throws MessagingException, UnsupportedEncodingException {
        if(!emailConfigProperties.isEnabled()) {
            log.info("Email sending is disabled");
            return;
        }
        String text = CONTENT.replace("${verifyURL}", buildRequestUrl(verificationCode));

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(emailConfigProperties.getFrom(), emailConfigProperties.getSender());
        helper.setTo(user.getEmail());
        helper.setSubject(emailConfigProperties.getSubject());
        helper.setText(text, true);

        mailSender.send(message);
    }

    private String buildRequestUrl(String verificationCode) {
        final String baseUrl = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        return baseUrl + LINK + verificationCode;
    }
}
