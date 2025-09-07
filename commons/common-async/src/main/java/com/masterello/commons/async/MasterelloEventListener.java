package com.masterello.commons.async;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.scheduling.annotation.Async;

@Slf4j
public abstract class MasterelloEventListener<T extends MasterelloEvent> implements ApplicationListener<T> {

    @Async
    @Override
    public void onApplicationEvent(T event) {
        log.info("Got event {} of type {}", event.getId(), event.getClass().getSimpleName());
        try {
            processEvent(event);
            log.info("Processed event {} of type {}", event.getId(), event.getClass().getSimpleName());
        } catch (Exception ex) {
            log.error("Failed to process event {} of type {}", event.getId(), event.getClass().getSimpleName());
            throw ex;
        }
    }

    protected abstract void processEvent(T event);
}
