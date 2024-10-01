package com.masterello.auth.controller;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.masterello.auth.AuthTestConfiguration;
import com.masterello.auth.dto.GoogleTokenInfo;
import com.masterello.commons.test.AbstractWebIntegrationTest;
import com.masterello.user.service.AuthNService;
import com.masterello.user.service.MasterelloUserService;
import com.masterello.user.value.MasterelloTestUser;
import com.masterello.user.value.MasterelloUser;
import com.masterello.user.value.Role;
import com.masterello.user.value.UserStatus;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
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

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.masterello.auth.config.AuthConstants.M_TOKEN_COOKIE;
import static com.masterello.auth.utils.AuthTestDataProvider.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SqlGroup({
        @Sql(scripts = {"classpath:sql/create-auth-test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = {"classpath:sql/clean-auth.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@WireMockTest(httpPort = 8111)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {AuthTestConfiguration.class})
public class Oauth2EndpointsIntegrationTest extends AbstractWebIntegrationTest {

    private static final String GOOGLE_TOKEN = "googoogle_token";
    public static final String USER_FROM_GOOGLE = "new_user_from_google@gmail.com";

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MasterelloUserService userService;
    @Autowired
    private AuthNService authNService;
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

        GoogleTokenInfo googleTokenInfo = GoogleTokenInfo.builder()
                .email(USER_2_EMAIL)
                .build();

        stubFor(post(urlPathEqualTo("/google/tokeninfo"))
                .withHeader("Content-Type", containing("application/json"))
                .withQueryParam("id_token", new EqualToPattern(GOOGLE_TOKEN, true))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(googleTokenInfo))));

        //@formatter:off
        RestAssured
                .given()
                    .header("Authorization", CLIENT_BEARER)
                    .accept("application/json")
                    .contentType("application/json")
                    .queryParam("grant_type", "google_oid")
                    .queryParam("token", GOOGLE_TOKEN)
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
        MasterelloUser user = userService.findByEmail(USER_2_EMAIL)
                .orElseThrow();

        assertEquals(USER_2_EMAIL, user.getEmail());
        assertFalse(user.isEmailVerified()); // shouldn't mark as verified existing user
        assertNotNull(user.getPassword());
    }

    @SneakyThrows
    @Test
    void getToken_with_googlegrant_signUp() {

        GoogleTokenInfo googleTokenInfo = GoogleTokenInfo.builder()
                .email(USER_FROM_GOOGLE)
                .build();

        stubFor(post(urlPathEqualTo("/google/tokeninfo"))
                .withHeader("Content-Type", containing("application/json"))
                .withQueryParam("id_token", new EqualToPattern(GOOGLE_TOKEN, true))
                .willReturn(ok()
                        .withHeader("Content-Type", "application/json")
                        .withBody(objectMapper.writeValueAsString(googleTokenInfo))));

        //@formatter:off
        RestAssured
                .given()
                    .header("Authorization", CLIENT_BEARER)
                    .accept("application/json")
                    .contentType("application/json")
                    .queryParam("grant_type", "google_oid")
                    .queryParam("token", GOOGLE_TOKEN)
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

        verify(authNService).googleSignup(USER_FROM_GOOGLE);
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
                    .body("userId", is(USER_1_ID.toString()))
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
                .body("userId", is(USER_1_ID.toString()))
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

     private static ValidatableResponse login(LoginRequest request) {
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
                        .contentType("application/json")
                        .queryParam("token", accessToken)
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

