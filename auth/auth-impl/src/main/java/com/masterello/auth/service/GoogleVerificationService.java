package com.masterello.auth.service;

import com.masterello.auth.dto.GoogleTokenInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class GoogleVerificationService {

    @Value("${masterello.auth.google.verification-url}")
    private String googleVerificationBaseUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public Optional<GoogleTokenInfo> verify(String token){

        // Build the URL with query parameters
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(googleVerificationBaseUrl + "/tokeninfo")
                .queryParam("id_token", token);

        // Create the HTTP headers
        HttpHeaders headers = new HttpHeaders();
        headers.put(HttpHeaders.CONTENT_TYPE, List.of(MediaType.APPLICATION_JSON_VALUE));

        // Create the HTTP entity with headers
        HttpEntity<String> entity = new HttpEntity<>(headers);
        try{
            ResponseEntity<GoogleTokenInfo> response = restTemplate.postForEntity(
                    builder.toUriString(),
                    entity,
                    GoogleTokenInfo.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return Optional.ofNullable(response.getBody());
            }
        } catch (Exception ex) {
            log.error("Google token verification failed", ex);
        }
        return Optional.empty();
    }

}
