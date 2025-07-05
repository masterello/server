package com.masterello.worker.listener;

import com.masterello.commons.async.MasterelloEventPublisher;
import com.masterello.commons.test.AbstractDBIntegrationTest;
import com.masterello.user.event.EmailVerifiedChangedEvent;
import com.masterello.user.value.MasterelloTestUser;
import com.masterello.worker.WorkerTestConfiguration;
import com.masterello.worker.domain.WorkerInfo;
import com.masterello.worker.repository.WorkerInfoRepository;
import com.masterello.worker.util.WorkerTestDataProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlGroup;

import java.util.Optional;

import static com.masterello.worker.util.WorkerTestDataProvider.WORKER_11;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SqlGroup({
        @Sql(scripts = "classpath:sql/create-worker-test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD),
        @Sql(scripts = "classpath:sql/clean.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
})
@SpringBootTest(classes = {WorkerTestConfiguration.class})
class EmailVerifiedChangedEventListenerIntegrationTest extends AbstractDBIntegrationTest {

    @Autowired
    private MasterelloEventPublisher publisher;
    @Autowired
    private WorkerInfoRepository workerInfoRepository;

    @Test
    void updateVerified_success() {
        Optional<WorkerInfo> worker = workerInfoRepository.findById(WORKER_11);
        assertFalse(worker.get().isVerified());
        MasterelloTestUser user = (MasterelloTestUser) WorkerTestDataProvider.getMasterelloTestUsers()
                .get(WORKER_11);
        user.setEmailVerified(true);
        publisher.publishEvent(new EmailVerifiedChangedEvent(user));
        worker = workerInfoRepository.findById(WORKER_11);
        assertTrue(worker.get().isVerified());
    }
}