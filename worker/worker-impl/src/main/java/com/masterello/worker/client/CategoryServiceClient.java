package com.masterello.worker.client;

import com.masterello.worker.client.dto.CategoryBulkRequest;
import com.masterello.worker.client.dto.CategoryDTO;
import com.masterello.worker.config.CategoryProperties;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class CategoryServiceClient {

    private final static String CHILD_PATH = "/categories/child-bulk";

    private final RestTemplate categoryRestTemplate;
    private final CategoryProperties categoryProperties;

    public CategoryServiceClient(@Qualifier("categoryRestTemplate") RestTemplate categoryRestTemplate, CategoryProperties categoryProperties) {
        this.categoryRestTemplate = categoryRestTemplate;
        this.categoryProperties = categoryProperties;
    }

    public List<Integer> getWithChildCategoryCodes(List<Integer> categories) {
        CategoryBulkRequest categoryBulkRequest = new CategoryBulkRequest(categories, true, true);
        HttpHeaders headers = new HttpHeaders();

        headers.set("Content-Type", "application/json");

        HttpEntity<Object> requestEntity = new HttpEntity<>(categoryBulkRequest, headers);
        ResponseEntity<Map<Integer, List<CategoryDTO>>> response = categoryRestTemplate.exchange(categoryProperties.getCategoryServiceUrl() + CHILD_PATH,
                HttpMethod.POST, requestEntity, new ParameterizedTypeReference<>() {
                });
        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            val children = response.getBody().values().stream()
                    .flatMap(List::stream)
                    .map(CategoryDTO::categoryCode);

            return Stream.concat(children, categories.stream())
                    .collect(Collectors.toList());
        } else {
            log.error("Failed to fetch public keys from passport service: {}", response.getStatusCode());
            return categories;
        }
    }
}
