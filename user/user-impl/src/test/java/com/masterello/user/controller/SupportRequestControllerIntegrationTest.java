package com.masterello.user.controller;

import com.masterello.auth.data.AuthData;
import com.masterello.auth.data.AuthZRole;
import com.masterello.auth.service.AuthService;
import com.masterello.commons.test.AbstractWebIntegrationTest;
import com.masterello.user.UserTestConfiguration;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.masterello.user.util.TestDataProvider.ACCESS_TOKEN;
import static com.masterello.user.util.TestDataProvider.VERIFIED_USER;
import static com.masterello.user.util.TestDataProvider.buildSupportRequestDto;
import static com.masterello.user.util.TestDataProvider.tokenCookie;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

@SqlGroup({
        @Sql(scripts = "classpath:sql/create-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "classpath:sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {UserTestConfiguration.class})
public class SupportRequestControllerIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private AuthService authService;

    @Test
    public void completeRequest() {
        mockAuth(VERIFIED_USER, List.of(AuthZRole.ADMIN), true);
        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                .when()
                    .post("/api/support/completeRequest/2d3c2abe-af52-4008-b147-6c816dbaba06")
                .then()
                    .statusCode(200);
        //@formatter:on
    }

    @Test
    public void completeRequestNoAuth() {
        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                .when()
                    .post("/api/support/completeRequest/2d3c2abe-af52-4008-b147-6c816dbaba06")
                .then()
                    .statusCode(403);
        //@formatter:on
    }
    @Test
    public void completeRequestUserAuth() {
        mockAuth(VERIFIED_USER, List.of(AuthZRole.USER), true);
        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                .when()
                    .post("/api/support/completeRequest/2d3c2abe-af52-4008-b147-6c816dbaba06")
                .then()
                .statusCode(401);
        //@formatter:on
    }

    @Test
    public void completeRequestNotFound() {
        mockAuth(VERIFIED_USER, List.of(AuthZRole.ADMIN), true);
        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                .when()
                    .post("/api/support/completeRequest/2d3c2abe-af52-4008-b147-6c816dbaba99")
                .then()
                    .statusCode(404);
        //@formatter:on
    }

    @Test
    public void createRequest() {
        var supportRequest = buildSupportRequestDto();

        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .body(supportRequest)
                .when()
                    .post("/api/support/contact")
                .then()
                    .statusCode(200);
        //@formatter:on
    }

    @Test
    public void retrieveUnprocessedRequests() {
        mockAuth(VERIFIED_USER, List.of(AuthZRole.ADMIN), true);
        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                .when()
                    .get("/api/support/getAllUnprocessedRequests")
                .then()
                    .statusCode(200)
                    .body("size()", is(1))
                    .body("[0].uuid", is("2d3c2abe-af52-4008-b147-6c816dbaba06"))
                    .body("[0].title", is("Support 3"))
                    .body("[0].email", is("test@test.com"))
                    .body("[0].processed", is(false))
                    .body("[0].message", is("Login is still not working!!!"))
                    .body("[0].phone", is("91213"));
        //@formatter:on
    }

    @Test
    public void retrieveUnprocessedRequestsUserAuth() {
        mockAuth(VERIFIED_USER, List.of(AuthZRole.USER), true);
        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                .when()
                    .get("/api/support/getAllUnprocessedRequests")
                .then()
                    .statusCode(401);
        //@formatter:on
    }

    @Test
    public void retrieveUnprocessedRequestsNoAuth() {
        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                .when()
                    .get("/api/support/getAllUnprocessedRequests")
                .then()
                    .statusCode(401);
        //@formatter:on
    }

    @Test
    public void retrieveAllSupportRequests() {
        mockAuth(VERIFIED_USER, List.of(AuthZRole.ADMIN), true);

        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                .when()
                    .get("/api/support/getAllRequests")
                .then()
                    .statusCode(200)
                    .body("size()", is(3));
        //@formatter:on
    }

    @Test
    public void retrieveAllSupportRequestsUserAuth() {
        mockAuth(VERIFIED_USER, List.of(AuthZRole.USER), true);

        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                .when()
                    .get("/api/support/getAllRequests")
                .then()
                    .statusCode(401);
        //@formatter:on
    }


    @Test
    public void retrieveAllSupportRequestsNoAuth() {
        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                .when()
                    .get("/api/support/getAllRequests")
                .then()
                    .statusCode(403);
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
