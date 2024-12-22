package com.masterello.worker.listener;

import com.masterello.user.event.UserStatusChangedEvent;
import com.masterello.user.value.MasterelloTestUser;
import com.masterello.user.value.Role;
import com.masterello.user.value.UserStatus;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.repository.WorkerInfoRepository;
import com.masterello.worker.util.WorkerTestDataProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static com.masterello.worker.util.WorkerTestDataProvider.WORKER_1;
import static com.masterello.worker.util.WorkerTestDataProvider.getWorkerInfo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserStatusChangedEventListenerTest {

    @Mock
    private WorkerInfoRepository workerInfoRepository;
    @InjectMocks
    private UserStatusChangedEventListener listener;

    @Test
    public void testListener_success() {
        MasterelloTestUser user = (MasterelloTestUser) WorkerTestDataProvider.getMasterelloTestUsers().get(WORKER_1);
        user.setStatus(UserStatus.BANNED);
        UserStatusChangedEvent event = new UserStatusChangedEvent(user);

        when(workerInfoRepository.findById(WORKER_1)).thenReturn(Optional.of(getWorkerInfo(WORKER_1)));
        listener.onApplicationEvent(event);

        verify(workerInfoRepository).findById(WORKER_1);
        WorkerInfo updatedWorkerInfo = getWorkerInfo(WORKER_1);
        updatedWorkerInfo.setActive(false);
        verify(workerInfoRepository).save(eq(updatedWorkerInfo));
    }

    @Test
    public void testListener_ignoreIfNotWorker() {
        MasterelloTestUser user = (MasterelloTestUser) WorkerTestDataProvider.getMasterelloTestUsers().get(WORKER_1);
        user.setRoles(Set.of(Role.USER));
        user.setStatus(UserStatus.BANNED);
        UserStatusChangedEvent event = new UserStatusChangedEvent(user);

        listener.onApplicationEvent(event);

        verifyNoInteractions(workerInfoRepository);
    }

    @Test
    public void testListener_ignoreIfStatusMatches() {
        MasterelloTestUser user = (MasterelloTestUser) WorkerTestDataProvider.getMasterelloTestUsers().get(WORKER_1);
        user.setStatus(UserStatus.BANNED);
        UserStatusChangedEvent event = new UserStatusChangedEvent(user);

        WorkerInfo workerInfo = getWorkerInfo(WORKER_1);
        workerInfo.setActive(false);
        when(workerInfoRepository.findById(WORKER_1)).thenReturn(Optional.of(workerInfo));
        listener.onApplicationEvent(event);

        verify(workerInfoRepository).findById(WORKER_1);
        verifyNoMoreInteractions(workerInfoRepository);
    }

}