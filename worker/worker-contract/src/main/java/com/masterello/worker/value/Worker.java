package com.masterello.worker.value;

import java.time.Instant;
import java.util.UUID;

public interface Worker {
    
    UUID getWorkerId();
    String getDescription();
    String getPhone();
    String getTelegram();
    String getWhatsapp();
    String getViber();
    boolean isActive();
    Instant getRegisteredAt();
}
