package com.masterello.auth.service;


import com.masterello.auth.repository.AuthorizationRequestEntityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthorizationRequestCleanupService {

    private final AuthorizationRequestEntityRepository authorizationRepository;

    @Scheduled(cron = "#{@cleanupSchedulerProperties.cron}")
    public void deleteExpiredAuthRequests() {
        authorizationRepository.deleteAllByExpiresAtBefore(Instant.now());
    }
}
