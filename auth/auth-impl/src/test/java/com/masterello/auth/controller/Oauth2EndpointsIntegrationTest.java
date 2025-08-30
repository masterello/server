package com.masterello.auth.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.masterello.auth.AuthTestConfiguration;
import com.masterello.auth.repository.AuthorizationRepository;
import com.masterello.commons.test.AbstractWebIntegrationTest;
import com.masterello.user.service.MasterelloUserService;
import com.masterello.user.value.MasterelloTestUser;
import com.masterello.user.value.Role;
import com.masterello.user.value.UserStatus;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.http.entity.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.masterello.auth.config.AuthConstants.M_TOKEN_COOKIE;
import static com.masterello.auth.config.AuthConstants.R_TOKEN_COOKIE;
import static com.masterello.auth.utils.AuthTestDataProvider.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SqlGroup({
        @Sql(scripts = {"classpath:sql/create-auth-test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = {"classpath:sql/clean-auth.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@WireMockTest(httpPort = 8111)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {AuthTestConfiguration.class})
public class Oauth2EndpointsIntegrationTest extends AbstractWebIntegrationTest {

    private static final String GOOGLE_AUTH_CODE = "XQ40Jy1ERz9QGWG2Lhl3CpjPfSLZ15FQyPCTafsL2dXxwZK3YVnVyZ-kW2Ov8t_riBjR_QUHX8QYmOHCo0Ica0bzUW68mf29c5yshIZCFrkGjwELHgE-HZarznEPBang";
    private static final String USED_GOOGLE_AUTH_CODE = "QqqqQqqqz9QGWG2Lhl3CpjPfSLZ15FQyPCTafsL2dXxwZK3YVnVyZ-kW2Ov8t_riBjR_QUHX8QYmOHCo0Ica0bzUW68mf29c5yshIZCFrkGjwELHgE-HZarznEPBig";
    
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MasterelloUserService userService;
    @Autowired
    private AuthorizationRepository authorizationRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @BeforeEach
    public void setUp() {
        MasterelloTestUser user1 = getUser(UUID.fromString(USER_1_ID), USER_1_EMAIL, USER_1_E_PASS, true);
        mockUser(user1);


        MasterelloTestUser user2 = getUser(UUID.fromString(USER_2_ID), USER_2_EMAIL, USER_2_E_PASS, false);
        mockUser(user2);
    }

    private void mockUser(MasterelloTestUser user) {
        when(userService.existsByEmail(user.getEmail()))
                .thenReturn(true);
        when(userService.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));
        when(userService.findById(user.getUuid()))
                .thenReturn(Optional.of(user));
    }

    @Test
    void getToken_with_passportgrant() {
        passwordEncoder.matches(USER_1_PASS, USER_1_E_PASS);
        LoginRequest request = new LoginRequest(USER_1_EMAIL, USER_1_PASS);
        //@formatter:off
        ValidatableResponse validatableResponse = login(request);

        validatableResponse
                    .body("access_token", notNullValue())
                    .body("refresh_token", notNullValue())
                    .body("token_type", is("Bearer"))
                    .body("roles", hasSize(1))
                    .body("roles", hasItem(Role.USER.name()));
        //@formatter:on
    }

    @Test
    void getToken_with_unknown_grant() {
        LoginRequest request = new LoginRequest(USER_1_EMAIL, USER_1_PASS);
        //@formatter:off
        RestAssured
                .given()
                    .header("Authorization", CLIENT_BEARER)
                    .accept("application/json")
                    .contentType("application/json")
                    .queryParam("grant_type", "unknown")
                    .body(request)
                .when()
                    .post("/oauth2/token")
                .then()
                    .statusCode(400);
        //@formatter:on
    }

    @Test
    void getToken_with_wrong_pass() {
        LoginRequest request = new LoginRequest(USER_1_EMAIL, "wrong password");
        //@formatter:off
        RestAssured
                .given()
                    .header("Authorization", CLIENT_BEARER)
                    .accept("application/json")
                    .contentType("application/json")
                    .queryParam("grant_type", "unknown")
                    .body(request)
                .when()
                    .post("/oauth2/token")
                .then()
                    .statusCode(400);
        //@formatter:on
    }

    @Test
    void getToken_with_wrong_client() {
        LoginRequest request = new LoginRequest(USER_1_EMAIL, USER_1_PASS);
        //@formatter:off
        RestAssured
                .given()
                    .header("Authorization", "wrong client")
                    .accept("application/json")
                    .contentType("application/json")
                    .queryParam("grant_type", "unknown")
                    .body(request)
                .when()
                    .post("/oauth2/token")
                .then()
                    .statusCode(401);
        //@formatter:on
    }

    @SneakyThrows
    @Test
    void getToken_with_googlegrant() {

        //@formatter:off
        RestAssured
                .given()
                    .header("Authorization", CLIENT_BEARER)
                    .accept("application/json")
                    .contentType(ContentType.APPLICATION_FORM_URLENCODED.getMimeType())
                    .queryParam("grant_type", "google_auth_code")
                    .formParam("token", GOOGLE_AUTH_CODE)
                .when()
                    .post("/oauth2/token")
                .then()
                    .statusCode(200)
                    .body("access_token", notNullValue())
                    .body("refresh_token", notNullValue())
                    .body("token_type", is("Bearer"))
                    .body("roles", hasSize(1))
                    .body("roles", hasItem(Role.USER.name()));
        //@formatter:on
    }

    @SneakyThrows
    @Test
    void getToken_with_googlegrant_fail_on_auth_code_reuse() {
        var authorization = authorizationRepository.findById("abda1cb3-885e-40e7-8dfc-50d1a468ac6a");
        assertTrue(authorization.isPresent());
         //@formatter:off
        RestAssured
                .given()
                    .header("Authorization", CLIENT_BEARER)
                    .accept("application/json")
                    .contentType("application/json")
                    .queryParam("grant_type", "google_auth_code")
                    .queryParam("token", USED_GOOGLE_AUTH_CODE)
                .when()
                    .post("/oauth2/token")
                .then()
                    .statusCode(401);
        //@formatter:on
        authorization = authorizationRepository.findById("abda1cb3-885e-40e7-8dfc-50d1a468ac6a");
        assertFalse(authorization.isPresent());
    }

    @Test
    void test_introspect() {
        LoginRequest request = new LoginRequest(USER_1_EMAIL, USER_1_PASS);
        //@formatter:off
        ValidatableResponse loginResponse = login(request);

        AccessTokenResponse tokens = loginResponse.extract()
                .as(AccessTokenResponse.class);

        ValidatableResponse introspectResponse = getIntrospectResponse(tokens.getAccessToken());

        introspectResponse
                    .body("userId", is(USER_1_ID))
                    .body("username", is(USER_1_EMAIL))
                    .body("emailVerified", is(true))
                    .body("userStatus", is(UserStatus.ACTIVE.name()))
                    .body("roles", hasSize(1))
                    .body("roles", hasItem(Role.USER.name()));
    }

    @Test
    void test_logout() {
        LoginRequest request = new LoginRequest(USER_1_EMAIL, USER_1_PASS);
        //@formatter:off
        ValidatableResponse loginResponse = login(request);

        AccessTokenResponse tokens = loginResponse.extract()
                .as(AccessTokenResponse.class);

        ValidatableResponse introspectResponse = getIntrospectResponse(tokens.getAccessToken());

        introspectResponse
                .body("active", is(true))
                .body("userId", is(USER_1_ID))
                .body("username", is(USER_1_EMAIL))
                .body("emailVerified", is(true))
                .body("userStatus", is(UserStatus.ACTIVE.name()))
                .body("roles", hasSize(1))
                .body("roles", hasItem(Role.USER.name()));

         RestAssured
                .given()
                    .header("Authorization", CLIENT_BEARER)
                    .cookie(M_TOKEN_COOKIE, tokens.getAccessToken())
                .when()
                    .post("/oauth2/logout")
                .then()
                    .statusCode(200);

        ValidatableResponse introspectResponseAfterLogout = getIntrospectResponse(tokens.getAccessToken());

        introspectResponseAfterLogout
                .body("active", is(false));
    }

    @Test
    void test_logout_withRefreshToken() {
        LoginRequest request = new LoginRequest(USER_1_EMAIL, USER_1_PASS);

        // Login and extract tokens
        ValidatableResponse loginResponse = login(request);
        AccessTokenResponse tokens = loginResponse.extract().as(AccessTokenResponse.class);

        // Introspect refresh token before logout
        ValidatableResponse introspectResponse = getIntrospectResponse(tokens.getAccessToken());

        introspectResponse
                .body("active", is(true))
                .body("userId", is(USER_1_ID))
                .body("username", is(USER_1_EMAIL))
                .body("emailVerified", is(true))
                .body("userStatus", is(UserStatus.ACTIVE.name()))
                .body("roles", hasSize(1))
                .body("roles", hasItem(Role.USER.name()));

        // Perform logout using refresh token cookie
        RestAssured
                .given()
                    .header("Authorization", CLIENT_BEARER)
                    .cookie(R_TOKEN_COOKIE, tokens.getRefreshToken())
                .when()
                    .post("/oauth2/logout")
                .then()
                    .statusCode(200);

        // Introspect refresh token after logout to confirm it's invalidated
        ValidatableResponse refreshIntrospectAfter = getIntrospectResponse(tokens.getRefreshToken());

        refreshIntrospectAfter
                .body("active", is(false));
    }


    @Test
    void refreshToken_invalidatesOldAccessToken() {
        // Step 1: Generate initial token pair
        LoginRequest request = new LoginRequest(USER_1_EMAIL, USER_1_PASS);
        ValidatableResponse initialResponse = login(request);

        String initialAccessToken = initialResponse.extract().path("access_token");
        String initialRefreshToken = initialResponse.extract().path("refresh_token");

        // Step 2: Use the refresh token to get a new token pair
        ValidatableResponse refreshedResponse = refreshToken(initialRefreshToken);

        String newAccessToken = refreshedResponse.extract().path("access_token");
        String newRefreshToken = refreshedResponse.extract().path("refresh_token");

        // Step 3: Validate that a new token pair was generated
        refreshedResponse
                .body("access_token", not(initialAccessToken))
                .body("refresh_token", not(initialRefreshToken))
                .body("token_type", is("Bearer"))
                .body("access_token", notNullValue())
                .body("refresh_token", notNullValue());

        // Step 4: Check that the initial access token is no longer valid
        ValidatableResponse introspectResponse = getIntrospectResponse(initialAccessToken);

        introspectResponse
                .body("active", is(false));
    }

    @Test
    void refreshToken_invalidatesAllTokensWhenRefreshReused() {
        // Step 1: Generate initial token pair
        LoginRequest request = new LoginRequest(USER_1_EMAIL, USER_1_PASS);
        ValidatableResponse initialResponse = login(request);

        String initialAccessToken = initialResponse.extract().path("access_token");
        String initialRefreshToken = initialResponse.extract().path("refresh_token");

        // Step 2: Use the refresh token to get a new token pair
        ValidatableResponse refreshedResponse = refreshToken(initialRefreshToken);

        String newAccessToken = refreshedResponse.extract().path("access_token");
        String newRefreshToken = refreshedResponse.extract().path("refresh_token");

        // Step 3: Validate that a new token pair was generated
        refreshedResponse
                .body("access_token", not(initialAccessToken))
                .body("refresh_token", not(initialRefreshToken))
                .body("token_type", is("Bearer"))
                .body("access_token", notNullValue())
                .body("refresh_token", notNullValue());

        // Step 4: Check that the initial access token is no longer valid
        ValidatableResponse introspectInitialTokenResponse = getIntrospectResponse(initialAccessToken);

        introspectInitialTokenResponse
                .body("active", is(false));

        // Step 4: Check that the new access token is valid
        ValidatableResponse introspectNewTokenResponse = getIntrospectResponse(newAccessToken);

        introspectNewTokenResponse
                .body("active", is(true));

        // Step 5: Reuse initial refresh token and check it fails
        ValidatableResponse refreshedAgainResponse = refreshToken(initialRefreshToken);

        refreshedAgainResponse.statusCode(401);

        // Step 5: Check latest access token is no longer valid
        ValidatableResponse introspectNewTokenAgainResponse = getIntrospectResponse(newAccessToken);

        introspectNewTokenAgainResponse
                .body("active", is(false));

        // Step 5: Reuse latest refresh token is also invalidated
        ValidatableResponse refreshNewTokenResponse = refreshToken(newRefreshToken);

        refreshNewTokenResponse.statusCode(401);
    }

    private ValidatableResponse refreshToken(String refreshToken) {
        //@formatter:off
        return RestAssured
                .given()
                    .header("Authorization", CLIENT_BEARER)
                    .contentType("application/x-www-form-urlencoded")
                    .formParam("grant_type", "refresh_token")
                    .formParam("refresh_token", refreshToken)
                .when()
                    .post("/oauth2/token")
                .then();
        //@formatter:on
    }

     private ValidatableResponse login(LoginRequest request) {
        //@formatter:off
        return RestAssured
                .given()
                    .header("Authorization", CLIENT_BEARER)
                    .accept("application/json")
                    .contentType("application/json")
                    .queryParam("grant_type", "password")
                    .body(request)
                .when()
                    .post("/oauth2/token")
                .then()
                    .statusCode(200);
        //@formatter:on
    }

    private static ValidatableResponse getIntrospectResponse(String accessToken) {
        return RestAssured
                .given()
                    .header("Authorization", CLIENT_BEARER)
                    .accept("application/json")
                    .contentType("application/x-www-form-urlencoded")
                    .formParam("token", accessToken)
                .when()
                    .post("/oauth2/introspect")
                .then()
                    .statusCode(200);
    }



    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginRequest {
        private String username;
        private String password;
    }


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AccessTokenResponse {
        @JsonProperty("access_token")
        private String accessToken;
        @JsonProperty("refresh_token")
        private String refreshToken;
        private Set<Role> roles;
        @JsonProperty("token_type")
        private String tokenType;
        @JsonProperty("expires_in")
        private Integer expiresIn;
    }
}

