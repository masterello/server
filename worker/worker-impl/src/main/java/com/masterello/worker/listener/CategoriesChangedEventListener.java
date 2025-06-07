package com.masterello.worker.listener;

import com.masterello.category.event.CategoriesChangedEvent;
import com.masterello.worker.validator.ServiceValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CategoriesChangedEventListener implements ApplicationListener<CategoriesChangedEvent> {


    private final ServiceValidator serviceValidator;

    @Override
    public void onApplicationEvent(@NotNull CategoriesChangedEvent event) {
        serviceValidator.init();
    }
}
