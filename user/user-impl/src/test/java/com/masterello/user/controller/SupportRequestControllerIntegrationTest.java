package com.masterello.user.controller;

import com.masterello.commons.test.AbstractWebIntegrationTest;
import com.masterello.user.UserTestConfiguration;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import static com.masterello.user.util.TestDataProvider.buildSupportRequestDto;
import static org.hamcrest.Matchers.is;

@SqlGroup({
        @Sql(scripts = "classpath:sql/create-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "classpath:sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {UserTestConfiguration.class})
public class SupportRequestControllerIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    public void completeRequest() {

        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                .when()
                    .post("/api/support/completeRequest/2d3c2abe-af52-4008-b147-6c816dbaba06")
                .then()
                    .statusCode(200);
        //@formatter:on
    }

    @Test
    public void completeRequestNotFound() {

        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
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

        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
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
    public void retrieveAllSupportRequests() {

        //@formatter:off
        RestAssured
                .given()
                .accept("application/json")
                .contentType("application/json")
                .when()
                .get("/api/support/getAllRequests")
                .then()
                .statusCode(200)
                .body("size()", is(3));
        //@formatter:on
    }

}
