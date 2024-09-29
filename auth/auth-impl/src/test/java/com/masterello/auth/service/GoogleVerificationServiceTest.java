package com.masterello.auth.service;

import com.masterello.auth.dto.GoogleTokenInfo;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(MockitoExtension.class)
class GoogleVerificationServiceTest {

    private GoogleVerificationService googleVerificationService;

    private MockRestServiceServer mockServer;

    private final String googleVerificationBaseUrl = "http://localhost:8080";

    @SneakyThrows
    @BeforeEach
    void setUp() {
        googleVerificationService = new GoogleVerificationService();
        RestTemplate restTemplate = (RestTemplate) FieldUtils.getField(GoogleVerificationService.class, "restTemplate", true)
                .get(googleVerificationService);
        mockServer = MockRestServiceServer.bindTo(restTemplate).build();
        FieldUtils.writeField(googleVerificationService, "googleVerificationBaseUrl", googleVerificationBaseUrl, true);
    }

    @Test
    void verify_WithValidToken_ShouldReturnGoogleTokenInfo() {
        // Arrange
        String token = "validToken";
        GoogleTokenInfo expectedTokenInfo = GoogleTokenInfo.builder()
                .email("email@masterello.com").build();

        // Mock the REST service response
        mockServer.expect(MockRestRequestMatchers.requestTo(googleVerificationBaseUrl + "/tokeninfo?id_token=" + token))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(withSuccess("{\"email\": \"email@masterello.com\"}", MediaType.APPLICATION_JSON));
        // Act
        Optional<GoogleTokenInfo> result = googleVerificationService.verify(token);

        // Assert
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isEqualTo(expectedTokenInfo);

        // Verify that the expected request was made
        mockServer.verify();
    }

    @Test
    void verify_WithInvalidToken_ShouldReturnEmptyOptional() {
        // Arrange
        String token = "invalidToken";

        // Mock the REST service response
        mockServer.expect(once(), MockRestRequestMatchers.requestTo(googleVerificationBaseUrl + "/tokeninfo?id_token=" + token))
                .andExpect(MockRestRequestMatchers.method(HttpMethod.POST))
                .andRespond(MockRestResponseCreators.withStatus(HttpStatus.BAD_REQUEST));

        // Act
        Optional<GoogleTokenInfo> result = googleVerificationService.verify(token);

        // Assert
        assertThat(result.isPresent()).isFalse();

        // Verify that the expected request was made
        mockServer.verify();
    }
}
