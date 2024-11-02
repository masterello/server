package com.masterello.auth.service;

import com.masterello.auth.config.CleanupSchedulerProperties;
import com.masterello.auth.repository.AuthorizationRepository;
import com.masterello.auth.repository.TokenPairRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthorizationCleanupServiceTest {

    private final int BATCH_SIZE = 5;

    @Mock
    private TokenPairRepository tokenPairRepository;

    @Mock
    private AuthorizationRepository authorizationRepository;
    @Spy
    private CleanupSchedulerProperties schedulerProperties = new CleanupSchedulerProperties(BATCH_SIZE, "0 0 0 * * ?");

    @InjectMocks
    private AuthorizationCleanupService cleanupService;

    @Test
    public void testCleanUpStaleSessions_NoExpiredTokens() {
        // Arrange
        OffsetDateTime now = OffsetDateTime.now();
        Page<String> emptyPage = new PageImpl<>(List.of());
        when(tokenPairRepository.findExpiredAuthorizationIds(any(), any())).thenReturn(emptyPage);

        // Act
        cleanupService.cleanUpStaleSessions();

        // Assert
        verify(tokenPairRepository, times(1)).findExpiredAuthorizationIds(any(), any());
        verify(authorizationRepository, never()).deleteAllById(any());
    }

    @Test
    public void testCleanUpStaleSessions_WithExpiredTokens() {
        // Arrange
        Page<String> expiredIdsPage1 = new PageImpl<>(List.of("authId1", "authId2"), PageRequest.of(0, BATCH_SIZE), 2);
        Page<String> expiredIdsPage2 = new PageImpl<>(List.of("authId3"), PageRequest.of(1, BATCH_SIZE), 1);

        when(tokenPairRepository.findExpiredAuthorizationIds(any(OffsetDateTime.class), any(PageRequest.class)))
                .thenReturn(expiredIdsPage1)
                .thenReturn(expiredIdsPage2)
                .thenReturn(new PageImpl<>(List.of())); // End with an empty page

        // Act
        cleanupService.cleanUpStaleSessions();

        // Assert
        ArgumentCaptor<Page<String>> captor = ArgumentCaptor.forClass(Page.class);
        verify(authorizationRepository, times(2)).deleteAllById(captor.capture());

        List<Page<String>> allCapturedArgs = captor.getAllValues();
        assertEquals(2, allCapturedArgs.size());
        assertTrue(allCapturedArgs.get(0).toList().contains("authId1"));
        assertTrue(allCapturedArgs.get(0).toList().contains("authId2"));
        assertTrue(allCapturedArgs.get(1).toList().contains("authId3"));
    }

    @Test
    public void testCleanUpStaleSessions_WithRetry_Success() {
        // Arrange
        Page<String> expiredIdsPage1 = new PageImpl<>(List.of("authId1", "authId2"), PageRequest.of(0, BATCH_SIZE), 2);
        Page<String> expiredIdsPage2 = new PageImpl<>(List.of("authId3"), PageRequest.of(1, BATCH_SIZE), 1);

        when(tokenPairRepository.findExpiredAuthorizationIds(any(OffsetDateTime.class), any(PageRequest.class)))
                .thenReturn(expiredIdsPage1)
                .thenThrow(new RuntimeException("failed to fetch"))
                .thenThrow(new RuntimeException("failed to fetch"))
                .thenReturn(expiredIdsPage2)
                .thenReturn(new PageImpl<>(List.of())); // End with an empty page

        // Act
        cleanupService.cleanUpStaleSessions();

        // Assert
        ArgumentCaptor<Page<String>> captor = ArgumentCaptor.forClass(Page.class);
        verify(authorizationRepository, times(2)).deleteAllById(captor.capture());

        List<Page<String>> allCapturedArgs = captor.getAllValues();
        assertEquals(2, allCapturedArgs.size());
        assertTrue(allCapturedArgs.get(0).toList().contains("authId1"));
        assertTrue(allCapturedArgs.get(0).toList().contains("authId2"));
        assertTrue(allCapturedArgs.get(1).toList().contains("authId3"));
    }

    @Test
    public void testCleanUpStaleSessions_WithRetry_PartialSuccess() {
        // Arrange
        Page<String> expiredIdsPage1 = new PageImpl<>(List.of("authId1", "authId2"), PageRequest.of(0, BATCH_SIZE), 2);
        Page<String> expiredIdsPage2 = new PageImpl<>(List.of("authId3"), PageRequest.of(1, BATCH_SIZE), 1);

        when(tokenPairRepository.findExpiredAuthorizationIds(any(OffsetDateTime.class), any(PageRequest.class)))
                .thenReturn(expiredIdsPage1)
                .thenThrow(new RuntimeException("failed to fetch"))
                .thenThrow(new RuntimeException("failed to fetch"))
                .thenThrow(new RuntimeException("failed to fetch"));

        // Act
        cleanupService.cleanUpStaleSessions();

        // Assert
        ArgumentCaptor<Page<String>> captor = ArgumentCaptor.forClass(Page.class);
        verify(authorizationRepository, times(1)).deleteAllById(captor.capture());

        List<Page<String>> allCapturedArgs = captor.getAllValues();
        assertEquals(1, allCapturedArgs.size());
        assertTrue(allCapturedArgs.get(0).toList().contains("authId1"));
        assertTrue(allCapturedArgs.get(0).toList().contains("authId2"));
    }
}