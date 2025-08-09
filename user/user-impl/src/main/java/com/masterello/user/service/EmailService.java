package com.masterello.user.service;

import com.masterello.commons.core.data.Locale;
import com.masterello.user.config.EmailConfigProperties;
import com.masterello.user.value.MasterelloUser;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {
    private final EmailConfigProperties emailConfigProperties;
    private final ResourceLoader resourceLoader;
    private static final String RESET_PASSWORD_LINK = "/user/reset-password/";

    private static final String CONFIRM_EMAIL_FILE = "classpath:email-templates/${locale}/confirm-email-short-code.html";

    private static final String RESET_PASSWORD_FILE = "classpath:email-templates/${locale}/reset-password.html";

    private final JavaMailSender mailSender;

    public void sendResetPasswordLink(@NonNull MasterelloUser user,
                                      @NonNull String resetLink,
                                      @NonNull Locale locale)
            throws MessagingException, IOException {

        String filePath = RESET_PASSWORD_FILE.replace("${locale}", locale.getCode());
        String text = readFile(filePath)
                .replace("${resetURL}", buildPasswordResetRequestUrl(resetLink, locale));
        sendMessage(user, text, emailConfigProperties.getResetPassSubject().get(locale));
    }

    public void sendConfirmationEmail(@NonNull MasterelloUser user, @NonNull String verificationCode,
                                      @Nullable Locale locale) throws MessagingException, IOException {
        Locale resolvedLocale = Optional.ofNullable(locale).orElse(Locale.EN);
        String filePath = CONFIRM_EMAIL_FILE.replace("${locale}", resolvedLocale.getCode());
        String text = readFile(filePath);
        text = text.replace("${code}", verificationCode);
        for (int i = 0; i < 6; i++) {
            String placeholder = "${code[" + i + "]}";
            String digit = i < verificationCode.length() ? String.valueOf(verificationCode.charAt(i)) : "";
            text = text.replace(placeholder, digit);
        }
        sendMessage(user, text, emailConfigProperties.getRegistrationSubject().get(resolvedLocale));
    }

    private String buildPasswordResetRequestUrl(String resetLinkToken, Locale locale) {
        return emailConfigProperties.getServiceUrl() + "/" + locale.getCode() + RESET_PASSWORD_LINK + resetLinkToken;
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

    private String readFile(String file) throws IOException {
        Resource resource = resourceLoader.getResource(file);
        byte[] bytes = resource.getInputStream().readAllBytes();
        return new String(bytes, StandardCharsets.UTF_8);
    }
}
