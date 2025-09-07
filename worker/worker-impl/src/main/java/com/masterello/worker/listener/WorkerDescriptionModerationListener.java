package com.masterello.worker.listener;

import com.masterello.commons.async.MasterelloEventListener;
import com.masterello.worker.event.DescriptionChangedEvent;
import com.masterello.worker.dto.ModerationResult;
import com.masterello.worker.service.ModerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class WorkerDescriptionModerationListener extends MasterelloEventListener<DescriptionChangedEvent> {

    private final ModerationService moderationService;

    @Override
    protected void processEvent(DescriptionChangedEvent event) {
        UUID workerId = event.getEntityId();
        String content = event.getNewValue();
        log.info("Moderation check for worker description, workerId={}", workerId);
        ModerationResult result = moderationService.moderateUserInput(content);
        if (!result.getDecision().equals(ModerationResult.Decision.ALLOW)) {
            log.warn("Moderation result for workerId={}:\nDecision: {}\nCategories: {}\nExplanation: {}",
                    workerId,
                    result.getDecision(),
                    String.join(", ", result.getCategories()),
                    result.getExplanation());
        } else {
            log.info("Moderation passed for workerId={}, decision={}", workerId, result.getDecision());
        }
    }
}
