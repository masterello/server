package com.masterello.user.service;

import com.masterello.commons.core.data.Locale;
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
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import static com.masterello.user.util.TestDataProvider.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class EmailServiceTest {

    @Mock
    ResourceLoader resourceLoader;
    @Mock
    private JavaMailSender mailSender;
    @Mock
    private EmailConfigProperties emailConfigProperties;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() throws IOException {
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        ServletRequestAttributes attrs = new ServletRequestAttributes(mockRequest);
        RequestContextHolder.setRequestAttributes(attrs);

        when(emailConfigProperties.isEnabled()).thenReturn(true);
        when(emailConfigProperties.getFrom()).thenReturn(FROM);
        when(emailConfigProperties.getSender()).thenReturn(SENDER);
        when(emailConfigProperties.getRegistrationSubject()).thenReturn(Map.of(Locale.EN, SUBJECT));
        when(emailConfigProperties.getResetPassSubject()).thenReturn(Map.of(Locale.EN, RESET_SUBJECT));

        Resource resource = mock(Resource.class);
        when(resourceLoader.getResource(any())).thenReturn(resource);
        String fakeContent = "Fake Template Content";
        InputStream fakeStream = new ByteArrayInputStream(fakeContent.getBytes(StandardCharsets.UTF_8));
        when(resource.getInputStream()).thenReturn(fakeStream);
    }

    @Test
    public void sendConfirmationEmail() throws MessagingException, IOException {
        //GIVEN
        var user = buildUser();
        var token = UUID.randomUUID().toString();
        var mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(eq(mimeMessage));

        //WHEN
        emailService.sendConfirmationEmail(user, token, null);

        //THEN
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(eq(mimeMessage));
    }

    @Test
    public void sendResetPasswordLink() throws MessagingException, IOException {
        //GIVEN
        var user = buildUser();
        var token = UUID.randomUUID().toString();
        var mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(eq(mimeMessage));

        //WHEN
        emailService.sendResetPasswordLink(user, token, LOCALE);

        //THEN
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(eq(mimeMessage));
    }
}
