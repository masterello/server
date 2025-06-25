package com.masterello.commons.async;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

@Getter
public abstract class MasterelloEvent extends ApplicationEvent {

    private final UUID id;

    public MasterelloEvent(Object source) {
        super(source);
        id = UUID.randomUUID();
    }
}
