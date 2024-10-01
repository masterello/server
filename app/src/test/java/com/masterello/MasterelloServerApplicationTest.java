package com.masterello;

import com.masterello.commons.test.AbstractWebIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = {MasterelloServerApplication.class})
class MasterelloServerApplicationTest extends AbstractWebIntegrationTest {

    @Test
    void contextLoads() {
    }
}