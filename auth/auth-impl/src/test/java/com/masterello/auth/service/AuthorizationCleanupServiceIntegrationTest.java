package com.masterello.auth.service;

import com.masterello.auth.AuthTestConfiguration;
import com.masterello.auth.domain.Authorization;
import com.masterello.auth.domain.TokenPair;
import com.masterello.auth.repository.AuthorizationRepository;
import com.masterello.auth.repository.TokenPairRepository;
import com.masterello.commons.test.AbstractDBIntegrationTest;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCollection;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SqlGroup({
        @Sql(scripts = {"classpath:sql/create-auth-cleanup-test-data.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = {"classpath:sql/clean-auth.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@SpringBootTest(classes = {AuthTestConfiguration.class})
public class AuthorizationCleanupServiceIntegrationTest extends AbstractDBIntegrationTest {

    @Autowired
    private TokenPairRepository tokenPairRepository;

    @Autowired
    private AuthorizationRepository authorizationRepository;

    @Autowired
    private AuthorizationCleanupService cleanupService;

    @Test
    public void testCleanUpStaleSessions() {
        // Arrange
        val count = authorizationRepository.count();
        assertEquals(10, count);
        val tokenCount = tokenPairRepository.count();
        assertEquals(16, tokenCount);

        // Act
        cleanupService.cleanUpStaleSessions();

        // Assert
        val newCount = authorizationRepository.count();
        assertEquals(5, newCount);
        val freshAuths = authorizationRepository.findAll().stream()
                .map(Authorization::getId)
                .toList();
        String[] expectedIds = new String[]{
                "550e8400-e29b-41d4-a716-446655440001",
                "550e8400-e29b-41d4-a716-446655440003",
                "550e8400-e29b-41d4-a716-446655440005",
                "550e8400-e29b-41d4-a716-446655440007",
                "550e8400-e29b-41d4-a716-446655440009"};
        assertThatCollection(freshAuths).containsExactlyInAnyOrder(expectedIds);

        val newTokenCount = tokenPairRepository.count();
        assertEquals(7, newTokenCount);
        val leftTokens = tokenPairRepository.findAll().stream()
                .map(TokenPair::getId)
                .map(UUID::toString)
                .toList();
        String[] expectedTokenIds = new String[]{
                "550e8400-e29b-41d4-a716-446655440102",
                "550e8400-e29b-41d4-a716-446655440106",
                "550e8400-e29b-41d4-a716-446655440107",
                "550e8400-e29b-41d4-a716-446655440109",
                "550e8400-e29b-41d4-a716-446655440111",
                "550e8400-e29b-41d4-a716-446655440112",
                "550e8400-e29b-41d4-a716-446655440116",
        };
        assertThatCollection(leftTokens).containsExactlyInAnyOrder(expectedTokenIds);

    }
}