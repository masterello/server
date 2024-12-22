package com.masterello.worker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.masterello.auth.data.AuthZRole;
import com.masterello.auth.extension.AuthMocked;
import com.masterello.category.dto.CategoryBulkRequest;
import com.masterello.category.dto.CategoryDto;
import com.masterello.category.service.ReadOnlyCategoryService;
import com.masterello.commons.test.AbstractWebIntegrationTest;
import com.masterello.user.service.MasterelloUserService;
import com.masterello.user.value.City;
import com.masterello.user.value.Country;
import com.masterello.user.value.MasterelloTestUser;
import com.masterello.user.value.MasterelloUser;
import com.masterello.worker.WorkerTestConfiguration;
import com.masterello.worker.domain.Language;
import com.masterello.worker.dto.PageRequestDTO;
import com.masterello.worker.dto.WorkerInfoDTO;
import com.masterello.worker.dto.WorkerSearchRequest;
import com.masterello.worker.dto.WorkerSearchResponse;
import com.masterello.worker.dto.WorkerServiceDTO;
import io.restassured.RestAssured;
import io.restassured.response.ValidatableResponse;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.masterello.worker.util.WorkerTestDataProvider.*;
import static org.hamcrest.Matchers.*;
import static org.hibernate.internal.util.collections.CollectionHelper.listOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.when;

@SqlGroup({
        @Sql(scripts = "classpath:sql/create-worker-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "classpath:sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {WorkerTestConfiguration.class})
class WorkerControllerIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private ReadOnlyCategoryService categoryService;
    @Autowired
    private MasterelloUserService userService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @AuthMocked(userId = WORKER_6_S, roles = {AuthZRole.WORKER})
    void storeWorkerInfo() {
        when(userService.findById(WORKER_6))
                .thenReturn(Optional.of(getMasterelloTestUsers().get(WORKER_6)));
        List<WorkerServiceDTO> services = List.of(new WorkerServiceDTO(10, 100),
                new WorkerServiceDTO(20, 200));

        WorkerInfoDTO info = WorkerInfoDTO.builder()
                .description(DESCRIPTION)
                .whatsapp(WHATSAPP)
                .telegram(TELEGRAM)
                .phone(PHONE)
                .country(Country.GERMANY)
                .city(City.BERLIN)
                .services(services)
                .build();

        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                    .body(info)
                .when()
                    .put("/api/worker/{uuid}/info", WORKER_6.toString())
                .then()
                    .statusCode(200)
                    .body("description", is(DESCRIPTION))
                    .body("whatsapp", is(WHATSAPP))
                    .body("telegram", is(TELEGRAM))
                    .body("phone", is(PHONE))
                    .body("viber", nullValue())
                    .body("services", hasSize(2))
                    .body("registeredAt", notNullValue())
                    .body("services", containsInAnyOrder(
                        Map.of("serviceId", 10, "amount", 100),
                        Map.of("serviceId", 20, "amount", 200)
                    ));
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER_6_S, roles = {AuthZRole.ADMIN})
    void storeWorkerInfo_by_admin() {
        when(userService.findById(WORKER_6))
                .thenReturn(Optional.of(getMasterelloTestUsers().get(WORKER_6)));
        List<WorkerServiceDTO> services = List.of(new WorkerServiceDTO(10, 100),
                new WorkerServiceDTO(20, 200));

        WorkerInfoDTO info = WorkerInfoDTO.builder()
                .description(DESCRIPTION)
                .whatsapp(WHATSAPP)
                .telegram(TELEGRAM)
                .phone(PHONE)
                .country(Country.GERMANY)
                .city(City.BERLIN)
                .services(services)
                .build();

        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                    .body(info)
                .when()
                    .put("/api/worker/{uuid}/info", WORKER_6.toString())
                .then()
                    .statusCode(200)
                    .body("description", is(DESCRIPTION))
                    .body("whatsapp", is(WHATSAPP))
                    .body("telegram", is(TELEGRAM))
                    .body("phone", is(PHONE))
                    .body("viber", nullValue())
                    .body("services", hasSize(2))
                    .body("services", containsInAnyOrder(
                            Map.of("serviceId", 10, "amount", 100),
                            Map.of("serviceId", 20, "amount", 200)
                    ));
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER_S)
    void storeWorkerInfo_fails_for_not_worker() {
        List<WorkerServiceDTO> services = List.of(new WorkerServiceDTO(10, 100),
                new WorkerServiceDTO(20, 200));

        WorkerInfoDTO info = WorkerInfoDTO.builder()
                .description(DESCRIPTION)
                .whatsapp(WHATSAPP)
                .telegram(TELEGRAM)
                .phone(PHONE)
                .country(Country.GERMANY)
                .city(City.BERLIN)
                .services(services)
                .build();

        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                    .body(info)
                .when()
                    .put("/api/worker/{uuid}/info", USER.toString())
                .then()
                    .statusCode(403);
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER_1_S, roles = {AuthZRole.WORKER})
    void getWorkerInfo() {

        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                .when()
                    .get("/api/worker/{uuid}/info", WORKER_1.toString())
                .then()
                    .statusCode(200)
                    .body("description", is("best plumber"))
                    .body("whatsapp", is("plumber-w"))
                    .body("telegram", is("plumber-t"))
                    .body("viber", is("plumber-v"))
                    .body("phone", is("+49111111111"))
                    .body("services", hasSize(1))
                    .body("services", containsInAnyOrder(
                            allOf(
                                    hasEntry("serviceId", 10),
                                    hasEntry("amount", 100)
                            )
                    ));

        //@formatter:on
    }

    @Test
    @AuthMocked(userId = ADMIN_S, roles = {AuthZRole.ADMIN})
    void getWorkerInfo_by_admin() {

        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                .when()
                    .get("/api/worker/{uuid}/info", WORKER_1.toString())
                .then()
                    .statusCode(200)
                    .body("description", is("best plumber"))
                    .body("whatsapp", is("plumber-w"))
                    .body("telegram", is("plumber-t"))
                    .body("viber", is("plumber-v"))
                    .body("phone", is("+49111111111"))
                    .body("services", hasSize(1))
                    .body("services",hasItem(
                            allOf(
                                    hasEntry("serviceId", 10),
                                    hasEntry("amount", 100)
                            ))
                    );
        //@formatter:on
    }

    @Test
    void getFullWorkerInfo() {
        when(userService.findById(WORKER_1))
                .thenReturn(Optional.of(MasterelloTestUser.builder()
                        .uuid(WORKER_1)
                        .title("Herr.")
                        .name("Plumber")
                        .lastname("Plumberson")
                        .build()));
        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                .when()
                    .get("/api/worker/{uuid}/full-info", WORKER_1.toString())
                .then()
                    .statusCode(200)
                    .body("uuid", is(WORKER_1.toString()))
                    .body("title", is("Herr."))
                    .body("name", is("Plumber"))
                    .body("lastname", is("Plumberson"))
                    .body("workerInfo.description", is("best plumber"))
                    .body("workerInfo.whatsapp", is("plumber-w"))
                    .body("workerInfo.telegram", is("plumber-t"))
                    .body("workerInfo.viber", is("plumber-v"))
                    .body("workerInfo.phone", is("+49111111111"))
                    .body("workerInfo.services", hasSize(1))
                    .body("workerInfo.city", is(City.HAMBURG.getCode()))
                    .body("workerInfo.services",hasItem(
                            allOf(
                                    hasEntry("serviceId", 10),
                                    hasEntry("amount", 100)
                            ))
                    )
                    .body("workerInfo.languages", hasSize(2))
                    .body("workerInfo.languages",
                            containsInAnyOrder(Language.RU.name(), Language.DE.name()));
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER_2_S, roles = {AuthZRole.WORKER})
    void patchWorkerInfoServices() {

        String body = "[{\"op\":\"replace\",\"path\":\"/services\"," +
                "\"value\":[{\"serviceId\":10, \"amount\": 250}, {\"serviceId\":15, \"amount\": 100}]}]";

        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .body(body)
                    .accept("application/json")
                    .contentType("application/json-patch+json")
                .when()
                    .patch("/api/worker/{uuid}/info", WORKER_2.toString())
                .then()
                    .statusCode(200)
                    .body("description", is("best electrician"))
                    .body("whatsapp", is("electrician-w"))
                    .body("telegram", is("electrician-t"))
                    .body("viber", is("electrician-v"))
                    .body("phone", is("+49222222222"))
                    .body("services", hasSize(2))
                    .body("services", containsInAnyOrder(
                            allOf(
                                    hasEntry("serviceId", 10),
                                    hasEntry("amount", 250)
                            ),
                            allOf(
                                    hasEntry("serviceId", 15),
                                    hasEntry("amount", 100)
                            )
                    ));
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = USER_S)
    void getWorkerInfo_fails_for_not_worker() {

        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .cookie(tokenCookie())
                .when()
                    .get("/api/worker/{uuid}/info", WORKER_1.toString())
                .then()
                    .statusCode(403);
        //@formatter:on
    }

    @Test
    @AuthMocked(userId = WORKER_2_S, roles = {AuthZRole.WORKER})
    void patchWorkerInfo() {

        String body = "[{\"op\":\"replace\",\"path\":\"/description\",\"value\":\"Not that good plumber\"}," +
                "{\"op\":\"replace\",\"path\":\"/phone\",\"value\":\"new phone\"}," +
                "{\"op\":\"replace\",\"path\":\"/whatsapp\",\"value\":\"new whatsapp\"}," +
                "{\"op\":\"replace\",\"path\":\"/telegram\",\"value\":\"new telegram\"}," +
                "{\"op\":\"replace\",\"path\":\"/viber\",\"value\":\"new viber\"}," +
                "{\"op\":\"add\",\"path\":\"/services/-\",\"value\":{\"serviceId\":20, \"amount\": 12}}," +
                "{\"op\":\"remove\",\"path\":\"/services/1\"}" +
                "]";
        //@formatter:off
        RestAssured
                .given()
                    .cookie(tokenCookie())
                    .body(body)
                    .accept("application/json")
                    .contentType("application/json-patch+json")
                .when()
                    .patch("/api/worker/{uuid}/info", WORKER_2.toString())
                .then()
                    .statusCode(200)
                    .body("description", is("Not that good plumber"))
                    .body("whatsapp", is("new whatsapp"))
                    .body("telegram", is("new telegram"))
                    .body("viber", is("new viber"))
                    .body("phone", is("new phone"))
                    .body("services", hasSize(2))
                    .body("services", containsInAnyOrder(
                            Map.of("serviceId", 10, "amount", 150),
                            Map.of("serviceId", 20, "amount", 12)
                    ));
        //@formatter:on
    }

    @SneakyThrows
    @ParameterizedTest
    @CsvSource({
            "ASC, workers_search_lang_and_services_asc.json",
            "DESC, workers_search_lang_and_services_desc.json"
    })
    void searchWorkers_by_lang_and_service(PageRequestDTO.SortOrder order, String expectedResponseFileName) {
        mockCategories(listOf(), Map.of());
        mockUsers(Set.of(WORKER_1, WORKER_2, WORKER_3));

        WorkerSearchRequest request = WorkerSearchRequest.builder()
                .languages(listOf(Language.DE, Language.EN))
                .services(listOf(10))
                .pageRequest(PageRequestDTO.builder()
                        .page(1)
                        .pageSize(10)
                        .sort(PageRequestDTO.Sort.builder()
                                .order(order)
                                .fields(List.of("registeredAt", "workerId"))
                                .build())
                        .build())
                .build();
        //@formatter:off
        String filePath = String.format("src/test/resources/responses/%s", expectedResponseFileName);
        val expectedResponse = readWorkerFromFile(filePath);
        val validatableResponse = RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                .body(request)
                .when()
                    .post("/api/worker/search")
                .then()
                    .statusCode(200);

        //@formatter:on

        val actualResponse = validatableResponse.extract().body().as(WorkerSearchResponse.class);
        assertEquals(expectedResponse, actualResponse);
    }

    private void mockUsers(Set<UUID> ids) {
        Map<UUID, MasterelloUser> users = getMasterelloTestUsers().entrySet().stream()
                .filter(u -> ids.contains(u.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        when(userService.findAllByIds(ids)).thenReturn(users);
    }

    @SneakyThrows
    @ParameterizedTest
    @CsvSource({
            "1, 2, workers_search_lang_desc_page_1_size_2.json",
            "2, 2, workers_search_lang_desc_page_2_size_2.json",
            "3, 2, workers_search_lang_desc_page_3_size_2.json",
            "1, 3, workers_search_lang_desc_page_1_size_3.json",
            "2, 3, workers_search_lang_desc_page_2_size_3.json"
    })
    void searchWorkers_by_lang_desc(int page, int pageSize, String expectedResponseFileName) {
        mockCategories(listOf(), Map.of());
        when(userService.findAllByIds(anySet())).thenReturn(getMasterelloTestUsers());

        WorkerSearchRequest request = WorkerSearchRequest.builder()
                .languages(listOf(Language.DE, Language.EN))
                .pageRequest(PageRequestDTO.builder()
                        .page(page)
                        .pageSize(pageSize)
                        .build())
                .build();

        // Construct the path to the expected response file
        String filePath = String.format("src/test/resources/responses/%s", expectedResponseFileName);
        WorkerSearchResponse expectedResponse = readWorkerFromFile(filePath);

        //@formatter:off
        ValidatableResponse validatableResponse = RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .body(request)
                .when()
                    .post("/api/worker/search")
                .then()
                    .statusCode(HttpStatus.OK.value());
        //@formatter:on

        WorkerSearchResponse actualResponse = validatableResponse.extract().body().as(WorkerSearchResponse.class);
        assertEquals(expectedResponse, actualResponse);
    }

    @SneakyThrows
    @ParameterizedTest
    @CsvSource({
            "1, 2, workers_search_service_page_1_size_2.json",
            "2, 2, workers_search_service_page_2_size_2.json",
            "3, 2, workers_search_service_page_3_size_2.json",
            "4, 2, workers_search_service_page_4_size_2.json"
    })
    void searchWorkers_by_service(int page, int pageSize, String expectedResponseFileName) {

        mockCategories(listOf(10), Map.of(10, List.of(randomCategory(11, 10), randomCategory(12, 10))));
        when(userService.findAllByIds(anySet())).thenReturn(getMasterelloTestUsers());

        WorkerSearchRequest request = WorkerSearchRequest.builder()
                .services(listOf(10))
                .pageRequest(PageRequestDTO.builder()
                        .page(page)
                        .pageSize(pageSize)
                        .sort(PageRequestDTO.Sort.builder()
                                .order(PageRequestDTO.SortOrder.ASC)
                                .fields(List.of("workerId"))
                                .build())
                        .build())
                .build();

        // Construct the path to the expected response file
        String filePath = String.format("src/test/resources/responses/%s", expectedResponseFileName);
        WorkerSearchResponse expectedResponse = readWorkerFromFile(filePath);

        //@formatter:off
        ValidatableResponse validatableResponse = RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .body(request)
                .when()
                    .post("/api/worker/search")
                .then()
                    .statusCode(HttpStatus.OK.value());
        //@formatter:on

        WorkerSearchResponse actualResponse = validatableResponse.extract().body().as(WorkerSearchResponse.class);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void searchWorkers_by_city() {
        when(userService.findAllByIds(anySet())).thenReturn(getMasterelloTestUsers());

        WorkerSearchRequest request = WorkerSearchRequest.builder()
                .cities(List.of(City.MUNICH, City.HAMBURG))
                .pageRequest(PageRequestDTO.builder()
                        .page(1)
                        .pageSize(10)
                        .sort(PageRequestDTO.Sort.builder()
                                .order(PageRequestDTO.SortOrder.ASC)
                                .fields(List.of("workerId"))
                                .build())
                        .build())
                .build();

        // Construct the path to the expected response file
        WorkerSearchResponse expectedResponse = readWorkerFromFile("src/test/resources/responses/workers_search_city_asc.json");

        //@formatter:off
        ValidatableResponse validatableResponse = RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .body(request)
                .when()
                    .post("/api/worker/search")
                .then()
                    .statusCode(HttpStatus.OK.value());
        //@formatter:on

        WorkerSearchResponse actualResponse = validatableResponse.extract().body().as(WorkerSearchResponse.class);
        assertEquals(expectedResponse, actualResponse);
    }


    @Test
    void searchWorkers_noRecords() {
        mockCategories(listOf(99), Map.of());

        WorkerSearchRequest request = WorkerSearchRequest.builder()
                .services(listOf(99))
                .pageRequest(PageRequestDTO.builder()
                        .page(1)
                        .pageSize(20)
                        .sort(PageRequestDTO.Sort.builder()
                                .order(PageRequestDTO.SortOrder.ASC)
                                .fields(List.of("workerId"))
                                .build())
                        .build())
                .build();

        WorkerSearchResponse expectedResponse = readWorkerFromFile("src/test/resources/responses/workers_search_service_empty.json");

        //@formatter:off
        ValidatableResponse validatableResponse = RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .body(request)
                .when()
                    .post("/api/worker/search")
                .then()
                    .statusCode(HttpStatus.OK.value());
        //@formatter:on

        WorkerSearchResponse actualResponse = validatableResponse.extract().body().as(WorkerSearchResponse.class);
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void searchWorkers_invalidSort() {
        mockCategories(listOf(10), Map.of());
        WorkerSearchRequest request = WorkerSearchRequest.builder()
                .services(listOf(10))
                .pageRequest(PageRequestDTO.builder()
                        .page(1)
                        .pageSize(1)
                        .sort(PageRequestDTO.Sort.builder()
                                .order(PageRequestDTO.SortOrder.ASC)
                                .fields(List.of("workerInfo.services.amount"))
                                .build())
                        .build())
                .build();

        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .body(request)
                .when()
                    .post("/api/worker/search")
                .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        //@formatter:on
    }

    @Test
    void searchWorkers_invalidPage() {
        mockCategories(listOf(10), Map.of());
        WorkerSearchRequest request = WorkerSearchRequest.builder()
                .services(listOf(10))
                .pageRequest(PageRequestDTO.builder()
                        .page(0)
                        .pageSize(10)
                        .sort(PageRequestDTO.Sort.builder()
                                .order(PageRequestDTO.SortOrder.ASC)
                                .fields(List.of("name"))
                                .build())
                        .build())
                .build();

        //@formatter:off
        RestAssured
                .given()
                    .accept("application/json")
                    .contentType("application/json")
                    .body(request)
                .when()
                    .post("/api/worker/search")
                .then()
                    .statusCode(HttpStatus.BAD_REQUEST.value());
        //@formatter:on
    }

    @SneakyThrows
    public WorkerSearchResponse readWorkerFromFile(String filePath) {
        return objectMapper.readValue(new File(filePath), WorkerSearchResponse.class);
    }

    private void mockCategories(List<Integer> categories, Map<Integer, List<CategoryDto>> response) {

        when(categoryService.getAllChildCategoriesBulk(new CategoryBulkRequest(categories, true)))
                .thenReturn(response);
    }
}