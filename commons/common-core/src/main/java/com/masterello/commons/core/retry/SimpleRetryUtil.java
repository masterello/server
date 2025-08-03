package com.masterello.commons.core.retry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryPolicy;
import org.springframework.retry.backoff.FixedBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Slf4j
public class SimpleRetryUtil {


    private static final long DEFAULT_BACKOFF_MS = 100;

    public static <T> T retryOperation(Supplier<T> operation, String operationName, int maxAttempts) throws Throwable {
        return retryWithPolicy(operation, operationName, maxAttempts, null);
    }

    public static <T> T retryOperationOn(Supplier<T> operation, String operationName,
                                         int maxAttempts,
                                         List<Class<? extends Throwable>> retryOnExceptions) throws Throwable {
        return retryWithPolicy(operation, operationName, maxAttempts, retryOnExceptions);
    }

    private static <T> T retryWithPolicy(Supplier<T> operation, String operationName,
                                         int maxAttempts,
                                         List<Class<? extends Throwable>> retryOnExceptions) throws Throwable {
        RetryTemplate retryTemplate = new RetryTemplate();
        retryTemplate.setRetryPolicy(createRetryPolicy(maxAttempts, retryOnExceptions));
        retryTemplate.setBackOffPolicy(defaultBackoff());

        return retryTemplate.execute((RetryCallback<T, Throwable>) context -> {
            log.info("Attempt {} of {} to {}", context.getRetryCount() + 1, maxAttempts, operationName);
            try {
                return operation.get();
            } catch (Throwable t) {
                log.warn("Exception on attempt {}: {}", context.getRetryCount() + 1, t.toString());
                throw t;
            }
        });
    }

    private static RetryPolicy createRetryPolicy(int maxAttempts,
                                                 List<Class<? extends Throwable>> retryOnExceptions) {
        if (retryOnExceptions == null || retryOnExceptions.isEmpty()) {
            return new SimpleRetryPolicy(maxAttempts);
        }

        Map<Class<? extends Throwable>, Boolean> retryableExceptions = retryOnExceptions.stream()
                .collect(Collectors.toMap(ex -> ex, ex -> true));
        return new SimpleRetryPolicy(maxAttempts, retryableExceptions, true);
    }

    private static FixedBackOffPolicy defaultBackoff() {
        FixedBackOffPolicy policy = new FixedBackOffPolicy();
        policy.setBackOffPeriod(DEFAULT_BACKOFF_MS);
        return policy;
    }
}
