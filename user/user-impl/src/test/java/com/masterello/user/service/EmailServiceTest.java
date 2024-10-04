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
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.UnsupportedEncodingException;
import java.util.Properties;
import java.util.UUID;

import static com.masterello.user.util.TestDataProvider.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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
        when(emailConfigProperties.getSubject()).thenReturn(SUBJECT);

    }

    @Test
    public void sendEmail() throws MessagingException, UnsupportedEncodingException {
        //GIVEN
        var user = buildUser();
        var token = UUID.randomUUID().toString();
        var mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(eq(mimeMessage));

        //WHEN
        emailService.sendEmail(user, token);

        //THEN
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(eq(mimeMessage));
    }
}
