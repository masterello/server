package com.masterello.translation.aspect

import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class Translated(
        val event: KClass<out TranslatedFieldChangedEvent<*,*,*>>
)
