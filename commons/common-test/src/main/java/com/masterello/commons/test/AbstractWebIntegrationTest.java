package com.masterello.commons.test;

import com.masterello.auth.extension.AuthMockExtension;
import com.masterello.auth.service.AuthService;
import io.restassured.RestAssured;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith({SpringExtension.class, AuthMockExtension.class})
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@Slf4j
public class AbstractWebIntegrationTest extends AbstractDBIntegrationTest{

    @MockBean
    protected JavaMailSender mailSender;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    protected AuthService authService;

    @LocalServerPort
    protected Integer port;

    @BeforeEach
    void globalSetUp() {
        RestAssured.baseURI = "http://localhost:" + port;
    }
}
