package com.masterello.user.event;

import com.masterello.user.value.MasterelloUser;
import org.springframework.context.ApplicationEvent;

public class UserStatusChangedEvent extends ApplicationEvent {

    public UserStatusChangedEvent(MasterelloUser updatedUser) {
        super(updatedUser);
    }

    public MasterelloUser getUpdatedUser() {
        return (MasterelloUser) getSource();
    }
}
