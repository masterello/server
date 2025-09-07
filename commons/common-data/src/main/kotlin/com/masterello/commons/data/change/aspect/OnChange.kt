package com.masterello.commons.data.change.aspect

import com.masterello.commons.data.change.event.FieldChangedEvent
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class OnChange(
        val event: KClass<out FieldChangedEvent<*, *, *>>
)

