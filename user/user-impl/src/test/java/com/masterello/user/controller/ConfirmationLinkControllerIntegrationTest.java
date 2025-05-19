package com.masterello.user.controller;

import com.masterello.commons.test.AbstractWebIntegrationTest;
import com.masterello.user.UserTestConfiguration;
import com.masterello.user.dto.ResendConfirmationLinkDTO;
import com.masterello.user.dto.VerifyUserTokenDTO;
import io.restassured.RestAssured;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.Properties;
import java.util.UUID;

import static com.masterello.user.util.TestDataProvider.tokenCookie;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SqlGroup({
        @Sql(scripts = "classpath:sql/create-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "classpath:sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {UserTestConfiguration.class})
public class ConfirmationLinkControllerIntegrationTest extends AbstractWebIntegrationTest {

    public static final String BASE_URL = "/api/user/confirmationLink";

    @Test
    public void verifyToken_token_not_found() {
        UUID token = UUID.randomUUID();
        VerifyUserTokenDTO tokenDTO = VerifyUserTokenDTO.builder().token(token.toString()).build();

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .accept("application/json")
                    .contentType("application/json")
                    .body(tokenDTO)
                .when()
                    .post(BASE_URL + "/verifyUserToken")
                .then()
                    .statusCode(404);
        //@formatter:on
    }

    @Test
    public void verifyToken_user_not_found() {
        UUID token = UUID.fromString("84e9798e-387a-11ee-be56-000000000002");
        VerifyUserTokenDTO tokenDTO = VerifyUserTokenDTO.builder().token(token.toString()).build();

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .accept("application/json")
                    .contentType("application/json")
                    .body(tokenDTO)
                .when()
                    .post(BASE_URL + "/verifyUserToken")
                .then()
                    .statusCode(404);
        //@formatter:on
    }

    @Test
    public void verifyToken_user_already_verified() {
        UUID token = UUID.fromString("84e9798e-387a-11ee-be56-0242ac120002");
        VerifyUserTokenDTO tokenDTO = VerifyUserTokenDTO.builder().token(token.toString()).build();

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .accept("application/json")
                    .contentType("application/json")
                    .body(tokenDTO)
                .when()
                    .post(BASE_URL + "/verifyUserToken")
                .then()
                    .statusCode(409);
        //@formatter:on
    }

    @Test
    public void verifyToken_token_expired() {
        UUID token = UUID.fromString("84e9798e-387a-11ee-be56-0242ac120099");
        VerifyUserTokenDTO tokenDTO = VerifyUserTokenDTO.builder().token(token.toString()).build();

        var mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(eq(mimeMessage));

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .accept("application/json")
                    .contentType("application/json")
                    .body(tokenDTO)
                .when()
                    .post(BASE_URL + "/verifyUserToken")
                .then()
                    .statusCode(400);
        //@formatter:on
    }

    @Test
    public void verifyToken() {
        UUID token = UUID.fromString("84e9798e-387a-11ee-be56-0242ac120011");
        VerifyUserTokenDTO tokenDTO = VerifyUserTokenDTO.builder().token(token.toString()).build();

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .accept("application/json")
                    .contentType("application/json")
                    .body(tokenDTO)
                .when()
                    .post(BASE_URL + "/verifyUserToken")
                .then()
                    .statusCode(200);
        //@formatter:on
    }

    @Test
    public void resendToken() {
        UUID userId = UUID.fromString("e8b0639f-148c-4f74-b834-bbe04072a416");
        ResendConfirmationLinkDTO confirmationLinkDTO = ResendConfirmationLinkDTO.builder()
                .userUuid(userId).build();

        var mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(eq(mimeMessage));

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .accept("application/json")
                    .contentType("application/json")
                    .body(confirmationLinkDTO)
                .when()
                    .post(BASE_URL + "/resendToken")
                .then()
                    .statusCode(200);
        //@formatter:on

        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    public void resendToken_user_not_found() {
        ResendConfirmationLinkDTO confirmationLinkDTO = ResendConfirmationLinkDTO.builder()
                .userUuid(UUID.randomUUID()).build();
        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .accept("application/json")
                    .contentType("application/json")
                    .body(confirmationLinkDTO)
                .when()
                    .post(BASE_URL + "/resendToken")
                .then()
                    .statusCode(404);
        //@formatter:on

        verifyNoInteractions(mailSender);
    }

    @Test
    public void resendToken_email_verified() {
        UUID userId = UUID.fromString("49200ea0-3879-11ee-be56-0242ac120002");
        ResendConfirmationLinkDTO confirmationLinkDTO = ResendConfirmationLinkDTO.builder()
                .userUuid(userId).build();

        var mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(eq(mimeMessage));

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .accept("application/json")
                    .contentType("application/json")
                    .body(confirmationLinkDTO)
                .when()
                    .post(BASE_URL + "/resendToken")
                .then()
                    .statusCode(200);
        //@formatter:on

        verifyNoInteractions(mailSender);
    }

    @Test
    public void resendToken_rate_limit() {
        UUID userId = UUID.fromString("e8b0639f-148c-4f74-b834-bbe04072a998");
        ResendConfirmationLinkDTO confirmationLinkDTO = ResendConfirmationLinkDTO.builder()
                .userUuid(userId).build();

        var mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(eq(mimeMessage));

        //@formatter:off
        RestAssured
                .given()
                .cookie(tokenCookie())
                .accept("application/json")
                .contentType("application/json")
                .body(confirmationLinkDTO)
                .when()
                .post(BASE_URL + "/resendToken")
                .then()
                .statusCode(429);
        //@formatter:on

        verifyNoInteractions(mailSender);
    }
}
