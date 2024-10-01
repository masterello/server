package com.masterello.user.controller;


import com.masterello.commons.test.AbstractWebIntegrationTest;
import com.masterello.user.UserTestConfiguration;
import com.masterello.user.value.Language;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.hamcrest.Matchers.containsInAnyOrder;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {UserTestConfiguration.class})
public class LanguagesControllerIntegrationTest extends AbstractWebIntegrationTest {

    @Test
    public void checkSupportedLanguages() {

        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                .when()
                    .get("/api/user/supported-languages")
                .then()
                    .statusCode(200)
                    .body("languages", containsInAnyOrder(Arrays.stream(Language.values()).map(Language::name).toArray()));

        //@formatter:on
    }
}
