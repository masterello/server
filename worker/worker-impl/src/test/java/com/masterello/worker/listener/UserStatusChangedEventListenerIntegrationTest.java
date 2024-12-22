package com.masterello.worker.listener;

import com.masterello.commons.test.AbstractDBIntegrationTest;
import com.masterello.user.event.UserStatusChangedEvent;
import com.masterello.user.value.MasterelloTestUser;
import com.masterello.user.value.UserStatus;
import com.masterello.worker.WorkerTestConfiguration;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.repository.WorkerInfoRepository;
import com.masterello.worker.util.WorkerTestDataProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.Optional;

import static com.masterello.worker.util.WorkerTestDataProvider.WORKER_1;
import static org.junit.jupiter.api.Assertions.*;

@SqlGroup({
        @Sql(scripts = "classpath:sql/create-worker-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "classpath:sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@SpringBootTest(classes = {WorkerTestConfiguration.class})
class UserStatusChangedEventListenerIntegrationTest extends AbstractDBIntegrationTest {

    @Autowired
    private ApplicationEventPublisher publisher;
    @Autowired
    private WorkerInfoRepository workerInfoRepository;

    @Test
    void updateStatus_success() {
        Optional<WorkerInfo> worker = workerInfoRepository.findById(WORKER_1);
        assertTrue(worker.get().isActive());
        MasterelloTestUser user = (MasterelloTestUser) WorkerTestDataProvider.getMasterelloTestUsers()
                .get(WORKER_1);
        user.setStatus(UserStatus.BANNED);
        publisher.publishEvent(new UserStatusChangedEvent(user));
        worker = workerInfoRepository.findById(WORKER_1);
        assertFalse(worker.get().isActive());
    }
}