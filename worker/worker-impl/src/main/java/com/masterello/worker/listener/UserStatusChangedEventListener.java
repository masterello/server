package com.masterello.worker.listener;

import com.masterello.user.event.UserStatusChangedEvent;
import com.masterello.user.value.MasterelloUser;
import com.masterello.user.value.Role;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.repository.WorkerInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserStatusChangedEventListener implements ApplicationListener<UserStatusChangedEvent> {


    private final WorkerInfoRepository workerInfoRepository;

    @Override
    public void onApplicationEvent(UserStatusChangedEvent event) {
        MasterelloUser user = event.getUpdatedUser();
        if(isWorker(user)) {
            Optional<WorkerInfo> workerOpt = workerInfoRepository.findById(user.getUuid());
            if(workerOpt.isEmpty()) {
                log.warn("WorkerInfo is not found for worker {}", user.getUuid());
                return;
            }
            WorkerInfo workerInfo = workerOpt.get();
            boolean isActive = user.isEnabled();
            if(isActive != workerInfo.isActive()) {
                workerInfo.setActive(isActive);
                workerInfoRepository.save(workerInfo);
            }
        }
    }

    private static boolean isWorker(MasterelloUser user) {
        return user.getRoles().contains(Role.WORKER);
    }
}
