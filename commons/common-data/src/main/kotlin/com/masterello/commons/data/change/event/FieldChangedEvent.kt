package com.masterello.commons.data.change.event

import com.masterello.commons.async.MasterelloEvent

abstract class FieldChangedEvent<S, I, T>(
        source: S,
        val entityId: I,
        val newValue: T
) : MasterelloEvent(source) {

}
