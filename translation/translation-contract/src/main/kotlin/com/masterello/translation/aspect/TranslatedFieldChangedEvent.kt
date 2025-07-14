package com.masterello.translation.aspect

import com.masterello.commons.async.MasterelloEvent

abstract class TranslatedFieldChangedEvent<S, I, T>(
        source: S,
        val entityId: I,
        val newValue: T
) : MasterelloEvent(source) {

}
