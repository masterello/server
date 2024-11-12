package com.masterello.user.controller;

import com.masterello.auth.data.AuthZRole;
import com.masterello.auth.extension.AuthMocked;
import com.masterello.auth.service.AuthService;
import com.masterello.commons.test.AbstractWebIntegrationTest;
import com.masterello.user.UserTestConfiguration;
import com.masterello.user.domain.MasterelloUserEntity;
import com.masterello.user.dto.AddRoleRequest;
import com.masterello.user.dto.SignUpRequest;
import com.masterello.user.dto.UpdatePasswordRequest;
import com.masterello.user.repository.UserRepository;
import com.masterello.user.value.Language;
import com.masterello.user.value.Role;
import io.restassured.RestAssured;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static com.masterello.user.util.TestDataProvider.*;
import static com.masterello.user.value.Role.USER;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

@SqlGroup({
        @Sql(scripts = "classpath:sql/create-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "classpath:sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {UserTestConfiguration.class})
public class UserControllerIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void signUp_successful() {
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .email("test_user@gmail.com")
                .password("Password123!")
                .build();

        var mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(eq(mimeMessage));

        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .body(signUpRequest)
                .when()
                    .post("/api/user/signup")
                .then()
                    .statusCode(201)
                    .body("email", is("test_user@gmail.com"))
                    .body("password", nullValue())
                    .body("uuid", notNullValue())
                    .body("roles", hasSize(1))
                    .body("roles", hasItem(USER.name()));

        //@formatter:on
    }

    @Test
    public void signUp_password_validation_failed() {
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .email("test_user@gmail.com")
                .password("password123")
                .build();

        var mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(eq(mimeMessage));

        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .body(signUpRequest)
                .when()
                    .post("/api/user/signup")
                .then()
                    .statusCode(400)
                    .body("errors", hasSize(2))
                    .body("errors", containsInAnyOrder(
                            Map.of("field", "password", "message", "Password must contain at least one special character"),
                            Map.of("field", "password", "message", "Password must contain at least one uppercase letter")
                    ));
        //@formatter:on
    }

    @Test
    public void signUp_email_empty() {
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .email("")
                .password("Password123!")
                .build();

        var mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(eq(mimeMessage));
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .body(signUpRequest)
                .when()
                    .post("/api/user/signup")
                .then()
                    .statusCode(400)
                    .body("errors", hasSize(1))
                    .body("errors[0].field", is("email"))
                    .body("errors[0].message", is("must not be empty"));
        //@formatter:on
    }

    @Test
    public void signUp_email_null() {
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .password("Password123!")
                .build();

        var mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(eq(mimeMessage));
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        //@formatter:off
        RestAssured
                .given()
                .accept("application/json")
                .contentType("application/json")
                .body(signUpRequest)
                .when()
                .post("/api/user/signup")
                .then()
                .statusCode(400)
                .body("errors", hasSize(1))
                .body("errors[0].field", is("email"))
                .body("errors[0].message", is("must not be empty"));
        //@formatter:on
    }

    @Test
    public void signUp_email_wrongformat() {
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .email("not_an_email@")
                .password("Password123!")
                .build();

        var mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        doNothing().when(mailSender).send(eq(mimeMessage));
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        //@formatter:off
        RestAssured
                .given()
                .accept("application/json")
                .contentType("application/json")
                .body(signUpRequest)
                .when()
                .post("/api/user/signup")
                .then()
                .statusCode(400)
                .body("errors", hasSize(1))
                .body("errors[0].field", is("email"))
                .body("errors[0].message", is("must be a well-formed email address"));
        //@formatter:on
    }

    @Test
    public void signUp_user_already_exists() {
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .email("verified@gmail.com")
                .password("Password123!")
                .build();

        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .body(signUpRequest)
                .when()
                    .post("/api/user/signup")
                .then()
                    .statusCode(409);

        //@formatter:on
    }

    @Test
    @AuthMocked(userId = VERIFIED_USER_S, roles = {AuthZRole.USER})
    public void addRole_successful() {
        MasterelloUserEntity masterelloUser = userRepository.findById(VERIFIED_USER).orElseThrow();
        assertEquals(1, masterelloUser.getRoles().size());
        assertTrue(masterelloUser.getRoles().contains(USER));

        AddRoleRequest request = AddRoleRequest.builder()
                .role(Role.WORKER).build();

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .accept("application/json")
                    .contentType("application/json")
                    .body(request)
                .when()
                    .post("/api/user/{uuid}/add-role", VERIFIED_USER.toString())
                .then()
                    .statusCode(200)
                    .body("email", is(VERIFIED_USER_EMAIL))
                    .body("uuid", is(VERIFIED_USER.toString()))
                    .body("roles", hasSize(2))
                    .body("roles", hasItem(USER.name()))
                    .body("roles", hasItem(Role.WORKER.name()));

        //@formatter:on
    }

    @Test
    @AuthMocked(userId = VERIFIED_USER_S, roles = {AuthZRole.USER})
    public void updatePassword_successful() {
        String newPassword = "newPassword123!";
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .oldPassword(VERIFIED_USER_PASS)
                .newPassword(newPassword)
                .build();

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .accept("application/json")
                    .contentType("application/json")
                    .body(request)
                .when()
                    .post("/api/user/{uuid}/password", VERIFIED_USER.toString())
                .then()
                    .statusCode(200);

        //@formatter:on

        val masterelloUserEntity = userRepository.findById(VERIFIED_USER).orElse(new MasterelloUserEntity());
        assertTrue(passwordEncoder.matches(newPassword, masterelloUserEntity.getPassword()));
    }

    @Test
    @AuthMocked(userId = VERIFIED_USER_2_S, roles = {AuthZRole.USER})
    public void updatePassword_same_password_failed() {
        String newPassword = "Qwerty123!";
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .oldPassword(newPassword)
                .newPassword(newPassword)
                .build();

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .accept("application/json")
                    .contentType("application/json")
                    .body(request)
                .when()
                    .post("/api/user/{uuid}/password", VERIFIED_USER_2.toString())
                .then()
                    .statusCode(400);

        //@formatter:on

        val masterelloUserEntity = userRepository.findById(VERIFIED_USER_2).orElse(new MasterelloUserEntity());
        assertTrue(passwordEncoder.matches(newPassword, masterelloUserEntity.getPassword()));
    }

    @Test
    @AuthMocked(userId = VERIFIED_USER_S, roles = {AuthZRole.USER})
    public void updatePassword_invalid() {
        String newPassword = "newPassword";
        UpdatePasswordRequest request = UpdatePasswordRequest.builder()
                .oldPassword("")
                .newPassword(newPassword)
                .build();

        //@formatter:off
         RestAssured
                .given()
                    .cookie(tokenCookie())
                    .accept("application/json")
                    .contentType("application/json")
                    .body(request)
                .when()
                    .post("/api/user/{uuid}/password", VERIFIED_USER.toString())
                .then()
                    .statusCode(400)
                         .body("errors", hasSize(3))
                         .body("errors", containsInAnyOrder(
                                 Map.of("field", "oldPassword", "message", "must not be empty"),
                                 Map.of("field", "newPassword", "message", "Password must contain at least one special character"),
                                 Map.of("field", "newPassword", "message", "Password must contain at least one digit")
                    ));

        //@formatter:on
    }

    @Test
    @AuthMocked(userId = VERIFIED_USER_S, roles = {AuthZRole.USER})
    public void addRole_existing_role() {
        AddRoleRequest request = AddRoleRequest.builder()
                .role(USER).build();

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .accept("application/json")
                    .contentType("application/json")
                    .body(request)
                .when()
                    .post("/api/user/{uuid}/add-role", VERIFIED_USER.toString())
                .then()
                    .statusCode(409);

        //@formatter:on
    }

    @Test
    @AuthMocked(userId = "ba7bb05a-80b3-41be-8182-66608aba2a33", roles = {AuthZRole.USER})
    public void addRole_user_not_found() {
        AddRoleRequest request = AddRoleRequest.builder()
                .role(USER).build();
        String userId = "ba7bb05a-80b3-41be-8182-66608aba2a33";

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .accept("application/json")
                    .contentType("application/json")
                    .body(request)
                .when()
                .post("/api/user/{uuid}/add-role", userId)
                .then()
                    .statusCode(404);

        //@formatter:on
    }

    @Test
    @AuthMocked(userId = VERIFIED_USER_S, roles = {AuthZRole.USER})
    public void retrieveUserByOwner() {

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                .when()
                    .get("/api/user/{uuid}", VERIFIED_USER.toString())
                .then()
                    .statusCode(200)
                    .body("uuid", is(VERIFIED_USER.toString()))
                    .body("email", is(VERIFIED_USER_EMAIL))
                    .body("name", is("test1"))
                    .body("lastname", is("user1"));
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = VERIFIED_USER_S, roles = {AuthZRole.USER})
    public void retrieveCurrentUser() {

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                .when()
                    .get("/api/user")
                .then()
                    .statusCode(200)
                    .body("uuid", is(VERIFIED_USER.toString()))
                    .body("email", is(VERIFIED_USER_EMAIL))
                    .body("name", is("test1"))
                    .body("lastname", is("user1"));
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = VERIFIED_USER_2_S, roles = {AuthZRole.ADMIN})
    public void retrieveUserByAdmin() {

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                .when()
                     .get("/api/user/{uuid}", VERIFIED_USER.toString())
                .then()
                    .statusCode(200)
                    .body("uuid", is(VERIFIED_USER.toString()))
                    .body("email", is(VERIFIED_USER_EMAIL))
                    .body("name", is("test1"))
                    .body("lastname", is("user1"));
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = VERIFIED_USER_2_S, roles = {AuthZRole.USER})
    public void retrieveUser_not_authorized() {
        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                .when()
                    .get("/api/user/{uuid}", VERIFIED_USER.toString())
                .then()
                    .statusCode(403);
        //@formatter:on
    }

    @Test
    public void retrieveUser_by_anonymous() {
        when(authService.validateToken(ACCESS_TOKEN)).thenReturn(Optional.empty());

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                .when()
                    .get("/api/user/{uuid}", VERIFIED_USER.toString())
                .then()
                    .statusCode(401);
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = "49200ea0-3879-11ee-be56-0242ac120009", roles = {AuthZRole.USER})
    public void retrieveUser_not_found() {

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                .when()
                    .get("/api/user/{uuid}", "49200ea0-3879-11ee-be56-0242ac120009")
                .then()
                    .statusCode(404);
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = "49200ea0-3879-11ee-be56-0242ac120009", roles = {AuthZRole.USER})
    public void patch_user_not_found() {
        String body = "[{\"op\":\"replace\",\"path\":\"/name\",\"value\":\"User\"},{\"op\":\"replace\",\"path\":\"/surname\",\"value\":\"Test\"}]";

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .body(body)
                    .accept("application/json")
                    .contentType("application/json-patch+json")
                .when()
                    .patch("/api/user/{uuid}", "49200ea0-3879-11ee-be56-0242ac120009")
                .then()
                    .statusCode(404);
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = VERIFIED_USER_S, roles = {AuthZRole.USER})
    public void patch_user() {
        String body = "[{\"op\":\"replace\",\"path\":\"/name\",\"value\":\"User\"},{\"op\":\"replace\",\"path\":\"/lastname\",\"value\":\"Test\"}]";

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .body(body)
                    .accept("application/json")
                    .contentType("application/json-patch+json")
                .when()
                    .patch("/api/user/{uuid}", VERIFIED_USER.toString())
                .then()
                    .statusCode(200)
                    .body("email", is(VERIFIED_USER_EMAIL))
                    .body("name", is("User"))
                    .body("lastname", is("Test"));
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = VERIFIED_USER_S, roles = {AuthZRole.USER})
    public void patch_user_add_language() {
        String body = "[{\"op\":\"add\",\"path\":\"/languages/-\",\"value\": \"EN\"}," +
                "{\"op\":\"add\",\"path\":\"/languages/-\",\"value\":\"UA\"}]";

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .body(body)
                    .accept("application/json")
                    .contentType("application/json-patch+json")
                .when()
                    .patch("/api/user/{uuid}", VERIFIED_USER.toString())
                .then()
                    .statusCode(200)
                    .body("languages", containsInAnyOrder(Language.EN.name(), Language.UA.name(), Language.DE.name(), Language.RU.name()));
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = VERIFIED_USER_S, roles = {AuthZRole.USER})
    public void patch_user_replace_language() {
        String body = "[{\"op\":\"replace\",\"path\":\"/languages\",\"value\": [\"EN\",  \"UA\"]}]";

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .body(body)
                    .accept("application/json")
                    .contentType("application/json-patch+json")
                .when()
                    .patch("/api/user/{uuid}", VERIFIED_USER.toString())
                .then()
                    .statusCode(200)
                    .body("languages", hasSize(2))
                    .body("languages", containsInAnyOrder(Language.EN.name(), Language.UA.name()));
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = VERIFIED_USER_S, roles = {AuthZRole.USER})
    public void patch_user_remove_language() {
        String body = "[{\"op\":\"remove\",\"path\":\"/languages/0\"}]";

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .body(body)
                    .accept("application/json")
                    .contentType("application/json-patch+json")
                .when()
                .patch("/api/user/{uuid}", VERIFIED_USER.toString())
                .then()
                .statusCode(200)
                .body("languages", contains(Language.DE.name()));
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = VERIFIED_USER_S, roles = {AuthZRole.USER})
    public void patch_user_forbidden_fields() {
        String body = "[{\"op\":\"replace\",\"path\":\"/name\",\"value\":\"User\"},{\"op\":\"replace\",\"path\":\"/email\",\"value\":\"Test\"}]";
        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .body(body)
                    .accept("application/json")
                    .contentType("application/json-patch+json")
                .when()
                    .patch("/api/user/{uuid}", VERIFIED_USER.toString())
                .then()
                .statusCode(400);
        //@formatter:on
    }
}
