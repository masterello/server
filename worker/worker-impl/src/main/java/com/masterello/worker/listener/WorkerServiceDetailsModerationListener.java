package com.masterello.worker.listener;

import com.masterello.commons.async.MasterelloEventListener;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.event.ServiceDetailsChangedEvent;
import com.masterello.worker.dto.ModerationResult;
import com.masterello.worker.service.ModerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class WorkerServiceDetailsModerationListener extends MasterelloEventListener<ServiceDetailsChangedEvent> {

    private final ModerationService moderationService;

    @Override
    protected void processEvent(ServiceDetailsChangedEvent event) {
        WorkerInfo workerInfo = (WorkerInfo) event.getSource();
        UUID workerId = workerInfo.getWorkerId();
        Integer serviceId = event.getEntityId();
        String content = event.getNewValue();
        log.info("Moderation check for service details, workerId={}, serviceId={}", workerId, serviceId);
        ModerationResult result = moderationService.moderateUserInput(content);
        if (!result.getDecision().equals(ModerationResult.Decision.ALLOW)) {
            log.warn("Moderation result for workerId={}/serviceId={}:\nDecision: {}\nCategories: {}\nExplanation: {}",
                    workerId,
                    serviceId,
                    result.getDecision(),
                    String.join(", ", result.getCategories()),
                    result.getExplanation());
        } else {
            log.info("Moderation passed for workerId={}/serviceId={}, decision={}", workerId, serviceId, result.getDecision());
        }
    }
}
