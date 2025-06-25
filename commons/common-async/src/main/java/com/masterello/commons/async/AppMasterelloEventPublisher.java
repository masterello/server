package com.masterello.commons.async;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class AppMasterelloEventPublisher implements MasterelloEventPublisher {

    private final ApplicationEventPublisher publisher;
    @Override
    public void publishEvent(MasterelloEvent event) {
        log.info("Publishing event {} of type {}", event.getId(), event.getClass().getSimpleName());
        try {
            publisher.publishEvent(event);
            log.info("Published event {} of type {}", event.getId(), event.getClass().getSimpleName());
        } catch (Exception ex) {
            log.error("Failed to publish event {} of type {}", event.getId(), event.getClass().getSimpleName());
            throw ex;
        }
    }
}
