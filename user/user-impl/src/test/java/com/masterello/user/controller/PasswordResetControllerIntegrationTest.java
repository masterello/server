package com.masterello.user.controller;

import com.masterello.commons.core.data.Locale;
import com.masterello.commons.test.AbstractWebIntegrationTest;
import com.masterello.user.UserTestConfiguration;
import com.masterello.user.domain.MasterelloUserEntity;
import com.masterello.user.dto.RequestPasswordResetDTO;
import com.masterello.user.dto.ResetPasswordDTO;
import com.masterello.user.repository.PasswordResetRepository;
import com.masterello.user.repository.UserRepository;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.Properties;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SqlGroup({
        @Sql(scripts = "classpath:sql/create-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "classpath:sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {UserTestConfiguration.class})
public class PasswordResetControllerIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetRepository passwordResetRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public static final String BASE_URL = "/api/passwordReset";

    @Test
    public void requestPasswordReset_user_not_found() {
        //@formatter:off
        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .body(RequestPasswordResetDTO.builder()
                        .userEmail("unknown@email.com")
                        .locale(Locale.EN)
                        .build())
                .when()
                    .post(BASE_URL + "/request")
                .then()
                    .statusCode(404);
        //@formatter:on
    }

    @Test
    public void requestPasswordReset_user_not_verified() {
        //@formatter:off
        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .body(RequestPasswordResetDTO.builder()
                        .userEmail("not_verified_link_valid@gmail.com")
                        .locale(Locale.EN)
                        .build())
                .when()
                    .post(BASE_URL + "/request")
                .then()
                    .statusCode(409);
        //@formatter:on
    }

    @Test
    public void requestPasswordReset_user_with_oauth() {
        //@formatter:off
        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .body(RequestPasswordResetDTO.builder()
                        .userEmail("oauth@gmail.com")
                        .locale(Locale.EN)
                        .build())
                .when()
                    .post(BASE_URL + "/request")
                .then()
                    .statusCode(406);
        //@formatter:on
    }

    @Test
    public void requestPasswordReset_rate_limit() {
        //@formatter:off
        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .body(RequestPasswordResetDTO.builder()
                        .userEmail("verified2@gmail.com")
                        .locale(Locale.EN)
                        .build())
                .when()
                    .post(BASE_URL + "/request")
                .then()
                    .statusCode(429);
        //@formatter:on
    }

    @Test
    public void requestPasswordReset() {

        var mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(eq(mimeMessage));

        //@formatter:off
        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .body(RequestPasswordResetDTO.builder()
                        .userEmail("verified@gmail.com")
                        .locale(Locale.EN)
                        .build())
                .when()
                    .post(BASE_URL + "/request")
                .then()
                    .statusCode(200);
        //@formatter:on

        verify(mailSender, times(1)).send(mimeMessage);

    }

    @Test
    public void changeUserPassword_not_found() {
        //@formatter:off
        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .body(ResetPasswordDTO.builder()
                        .password("StrongPass123!")
                        .token("test54")
                        .build())
                .when()
                    .post(BASE_URL)
                .then()
                    .statusCode(404);
        //@formatter:on
    }

    @Test
    public void changeUserPassword_link_expired() {
        //@formatter:off
        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .body(ResetPasswordDTO.builder()
                        .password("StrongPass123!")
                        .token("test1")
                        .build())
                .when()
                    .post(BASE_URL)
                .then()
                    .statusCode(400);
        //@formatter:on
    }

    @Test
    public void changeUserPassword_not_valid_password() {
        //@formatter:off
        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .body(ResetPasswordDTO.builder()
                        .password("invalid")
                        .token("test2")
                        .build())
                .when()
                    .post(BASE_URL)
                .then()
                    .statusCode(400);
        //@formatter:on
    }

    @Test
    public void changeUserPassword() {
        var userId = UUID.fromString("ba7bb05a-80b3-41be-8182-66608aba2a31");
        var password = "StrongPass123!";
        //@formatter:off
        RestAssured
                .given()
                    .contentType(ContentType.JSON)
                    .body(ResetPasswordDTO.builder()
                        .password(password)
                        .token("test2")
                        .build())
                .when()
                    .post(BASE_URL)
                .then()
                    .statusCode(200);
        //@formatter:on

        var count = passwordResetRepository.findResetCountsByUserUuid(userId);
        var user = userRepository.findById(userId).orElse(new MasterelloUserEntity());

        assertEquals(0, count);
        assertTrue(passwordEncoder.matches(password, user.getPassword()));
    }
}
