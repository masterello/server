package com.masterello.user.service;

import com.masterello.user.config.EmailConfigProperties;
import jakarta.mail.MessagingException;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.UUID;

import static com.masterello.user.util.TestDataProvider.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;
    @Mock
    private EmailConfigProperties emailConfigProperties;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        ServletRequestAttributes attrs = new ServletRequestAttributes(mockRequest);
        RequestContextHolder.setRequestAttributes(attrs);

        when(emailConfigProperties.isEnabled()).thenReturn(true);
        when(emailConfigProperties.getFrom()).thenReturn(FROM);
        when(emailConfigProperties.getSender()).thenReturn(SENDER);
        when(emailConfigProperties.getRegistrationSubject()).thenReturn(SUBJECT);
        when(emailConfigProperties.getResetPassSubject()).thenReturn(RESET_SUBJECT);

    }

    @Test
    public void sendConfirmationEmail() throws MessagingException, UnsupportedEncodingException {
        //GIVEN
        var user = buildUser();
        var token = UUID.randomUUID().toString();
        var mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(eq(mimeMessage));

        //WHEN
        emailService.sendConfirmationEmail(user, token);

        //THEN
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(eq(mimeMessage));
    }

    @Test
    public void sendResetPasswordLink() throws MessagingException, UnsupportedEncodingException {
        //GIVEN
        var user = buildUser();
        var token = UUID.randomUUID().toString();
        var mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(eq(mimeMessage));

        //WHEN
        emailService.sendResetPasswordLink(user, token);

        //THEN
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(eq(mimeMessage));
    }
}
