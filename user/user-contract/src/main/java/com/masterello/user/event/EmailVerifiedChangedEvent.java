package com.masterello.user.event;

import com.masterello.commons.async.MasterelloEvent;
import com.masterello.user.value.MasterelloUser;

public class EmailVerifiedChangedEvent extends MasterelloEvent {

    public EmailVerifiedChangedEvent(MasterelloUser updatedUser) {
        super(updatedUser);
    }

    public MasterelloUser getUpdatedUser() {
        return (MasterelloUser) getSource();
    }
}
