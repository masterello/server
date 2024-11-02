package com.masterello.auth.service;


import com.masterello.auth.config.CleanupSchedulerProperties;
import com.masterello.auth.repository.AuthorizationRepository;
import com.masterello.auth.repository.TokenPairRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Collections;

import static com.masterello.commons.core.retry.SimpleRetryUtil.retryOperation;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthorizationCleanupService {

    private static final int MAX_RETRIES = 3;
    private static final int PAGE_ZERO = 0;
    private final TokenPairRepository tokenPairRepository;
    private final AuthorizationRepository authorizationRepository;
    private final CleanupSchedulerProperties properties;


    @Scheduled(cron = "#{@cleanupSchedulerProperties.cron}")
    public void cleanUpStaleSessions() {
        log.info("Start sessions cleanup");
        OffsetDateTime now = OffsetDateTime.now();
        Page<String> expiredAuthorizationIds;
        int page = 0;
        do {
            expiredAuthorizationIds = processBatchWithRetry(page, now);
            page++;
        } while (!expiredAuthorizationIds.isEmpty());
        log.info("Finished sessions cleanup");
    }

    private Page<String> processBatchWithRetry(int page, OffsetDateTime now) {
        Page<String> expiredAuthorizationIds;
        try {
            expiredAuthorizationIds = retryOperation(() -> processBatch(page, now), MAX_RETRIES);
        } catch(Throwable ex) {
            log.error("Couldn't process page {}", page, ex);
            expiredAuthorizationIds = new PageImpl<>(Collections.emptyList());
        }
        return expiredAuthorizationIds;
    }

    private Page<String> processBatch(int page, OffsetDateTime now) {
        try{
            log.info("Fetching page {}", page);
            // In fact we always fetch the page 0, cause at every iteration we delete the items which shifts the rest up
            Page<String> expiredAuthorizationIds = tokenPairRepository.findExpiredAuthorizationIds(now,
                    PageRequest.of(PAGE_ZERO, properties.getBatchSize()));
            log.info("Fetched page {}, count: {}", page, expiredAuthorizationIds.stream().count());
            log.debug("Fetched page {}, ids: {}", page, expiredAuthorizationIds.get().toList());
            if (!expiredAuthorizationIds.isEmpty()) {
                authorizationRepository.deleteAllById(expiredAuthorizationIds);
            }
            return expiredAuthorizationIds;
        } catch(Throwable ex) {
            log.error("Failed to process page: {}. Retry if possible", page, ex);
            throw ex;
        }
    }
}
