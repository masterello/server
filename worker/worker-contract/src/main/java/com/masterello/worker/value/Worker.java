package com.masterello.worker.value;

import com.masterello.user.value.City;
import com.masterello.user.value.Country;

import java.time.Instant;
import java.util.UUID;

public interface Worker {
    
    UUID getWorkerId();
    String getDescription();
    String getPhone();
    String getTelegram();
    String getWhatsapp();
    String getViber();
    Country getCountry();
    City getCity();
    boolean isActive();
    Instant getRegisteredAt();
}
