package com.masterello.user.controller;


import com.masterello.auth.data.AuthData;
import com.masterello.auth.data.AuthZRole;
import com.masterello.auth.service.AuthService;
import com.masterello.commons.test.AbstractWebIntegrationTest;
import com.masterello.user.UserTestConfiguration;
import com.masterello.user.domain.MasterelloUserEntity;
import com.masterello.user.dto.AddRoleRequest;
import com.masterello.user.dto.SignUpRequest;
import com.masterello.user.repository.UserRepository;
import com.masterello.user.value.Language;
import com.masterello.user.value.Role;
import io.restassured.RestAssured;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;

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

    @Test
    public void signUp_successful() {
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
                    .statusCode(201)
                    .body("email", is("test_user@gmail.com"))
                    .body("password", nullValue())
                    .body("uuid", notNullValue())
                    .body("roles", hasSize(1))
                    .body("roles", hasItem(USER.name()));

        //@formatter:on
    }

    @Test
    public void signUp_user_already_exists() {
        SignUpRequest signUpRequest = SignUpRequest.builder()
                .email("verified@gmail.com")
                .password("password123")
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
    public void addRole_successful() {
        MasterelloUserEntity masterelloUser = userRepository.findById(VERIFIED_USER).orElseThrow();
        assertEquals(1, masterelloUser.getRoles().size());
        assertTrue(masterelloUser.getRoles().contains(USER));

        AddRoleRequest request = AddRoleRequest.builder()
                .role(Role.WORKER).build();

        mockAuth(VERIFIED_USER, List.of(AuthZRole.USER), true);

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
    public void addRole_existing_role() {
        AddRoleRequest request = AddRoleRequest.builder()
                .role(USER).build();
        mockAuth(VERIFIED_USER, List.of(AuthZRole.USER), true);

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
    public void addRole_user_not_found() {
        AddRoleRequest request = AddRoleRequest.builder()
                .role(USER).build();
        UUID user = UUID.randomUUID();
        mockAuth(user, List.of(AuthZRole.USER), true);

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .accept("application/json")
                    .contentType("application/json")
                    .body(request)
                .when()
                .post("/api/user/{uuid}/add-role", user.toString())
                .then()
                    .statusCode(404);

        //@formatter:on
    }

    @Test
    public void retrieveUserByOwner() {
        mockAuth(VERIFIED_USER, List.of(AuthZRole.USER), true);

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
    public void retrieveCurrentUser() {
        mockAuth(VERIFIED_USER, List.of(AuthZRole.USER), true);

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                .when()
                    .get("/api/user/")
                .then()
                    .statusCode(200)
                    .body("uuid", is(VERIFIED_USER.toString()))
                    .body("email", is(VERIFIED_USER_EMAIL))
                    .body("name", is("test1"))
                    .body("lastname", is("user1"));
        //@formatter:on
    }

    @Test
    public void retrieveUserByAdmin() {
        mockAuth(UUID.randomUUID(), List.of(AuthZRole.ADMIN), false);

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
    public void retrieveUser_not_authorized() {
        UUID randomUser = UUID.randomUUID();
        mockAuth(randomUser, List.of(AuthZRole.USER), true);

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
    public void retrieveUser_by_anonymous() {
        when(authService.validateToken(ACCESS_TOKEN)).thenReturn(Optional.empty());

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
    public void retrieveUser_not_found() {
        UUID user = UUID.randomUUID();
        mockAuth(user, List.of(AuthZRole.USER), true);

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                .when()
                    .get("/api/user/{uuid}", user.toString())
                .then()
                    .statusCode(404);
        //@formatter:on
    }

    @Test
    public void patch_user_not_found() {
        UUID user = UUID.randomUUID();
        String body = "[{\"op\":\"replace\",\"path\":\"/name\",\"value\":\"User\"},{\"op\":\"replace\",\"path\":\"/surname\",\"value\":\"Test\"}]";
        mockAuth(user, List.of(AuthZRole.USER), true);

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .body(body)
                    .accept("application/json")
                    .contentType("application/json-patch+json")
                .when()
                    .patch("/api/user/{uuid}", user.toString())
                .then()
                    .statusCode(404);
        //@formatter:on
    }

    @Test
    public void patch_user() {
        String body = "[{\"op\":\"replace\",\"path\":\"/name\",\"value\":\"User\"},{\"op\":\"replace\",\"path\":\"/lastname\",\"value\":\"Test\"}]";
        mockAuth(VERIFIED_USER, List.of(AuthZRole.USER), true);

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
    public void patch_user_add_language() {
        String body = "[{\"op\":\"add\",\"path\":\"/languages/-\",\"value\": \"EN\"}," +
                "{\"op\":\"add\",\"path\":\"/languages/-\",\"value\":\"UA\"}]";
        mockAuth(VERIFIED_USER, List.of(AuthZRole.USER), true);

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
    public void patch_user_replace_language() {
        String body = "[{\"op\":\"replace\",\"path\":\"/languages\",\"value\": [\"EN\",  \"UA\"]}]";
        mockAuth(VERIFIED_USER, List.of(AuthZRole.USER), true);

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
    public void patch_user_remove_language() {
        String body = "[{\"op\":\"remove\",\"path\":\"/languages/0\"}]";
        mockAuth(VERIFIED_USER, List.of(AuthZRole.USER), true);

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
    public void patch_user_forbidden_fields() {
        String body = "[{\"op\":\"replace\",\"path\":\"/name\",\"value\":\"User\"},{\"op\":\"replace\",\"path\":\"/email\",\"value\":\"Test\"}]";
        mockAuth(VERIFIED_USER, List.of(AuthZRole.USER), true);
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

    private void mockAuth(UUID userId, List<AuthZRole> roles, boolean emailVerified) {
        when(authService.validateToken(ACCESS_TOKEN))
                .thenReturn(Optional.of(AuthData.builder()
                                .userId(userId)
                                .userRoles(roles)
                                .emailVerified(emailVerified)
                        .build()));
    }
}
