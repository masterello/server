package com.masterello.translation.aspect

import com.masterello.commons.async.MasterelloEventPublisher
import jakarta.persistence.ElementCollection
import jakarta.persistence.EntityManager
import jakarta.persistence.Id
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut
import org.springframework.stereotype.Component
import java.lang.reflect.Field
import kotlin.reflect.KClass


@Aspect
@Component
class TranslationAspect(private val entityManager: EntityManager,
                        private val eventPublisher: MasterelloEventPublisher
) {

    @Pointcut("execution(* org.springframework.data.jpa.repository.JpaRepository+.save*(..)) || execution(* org.springframework.data.jpa.repository.JpaRepository+.saveAndFlush(..))")
    fun repositorySaveMethods() {
    }

    @Around("repositorySaveMethods()")
    fun aroundSave(pjp: ProceedingJoinPoint): Any? {
        val entity = pjp.args.firstOrNull() ?: return pjp.proceed()

        val id = getEntityId(entity) ?: return pjp.proceed()

        val existing = entityManager.find(entity.javaClass, id)

        for (field in entity.javaClass.declaredFields) {
            field.isAccessible = true

            val translated = field.getAnnotation(Translated::class.java)
            val translatedCollection = field.getAnnotation(TranslatedCollection::class.java)
            if (translated != null || translatedCollection != null) {
                if (field.isAnnotationPresent(ElementCollection::class.java)) {
                    processEmbeddedCollections(field, existing, entity)
                    continue
                }
                val oldValue = existing?.let { field.get(it) }
                val newValue = field.get(entity)
                if (oldValue != newValue) {
                    fireTranslatedEvent(translated.event, entity, id, newValue)
                }
            }
        }
        return pjp.proceed()
    }

    private fun processEmbeddedCollections(field: Field, existing: Any?, entity: Any) {
        val oldCollection = existing?.let {field.get(existing) as? Collection<*>}
        val newCollection = field.get(entity) as? Collection<*>

        val oldMap = oldCollection?.associateBy { getEmbeddedKey(it!!) } ?: emptyMap()
        val newMap = newCollection?.associateBy { getEmbeddedKey(it!!) } ?: emptyMap()
        val keys = oldMap.keys + newMap.keys
        for (key in keys) {
            val oldItem = oldMap[key]
            val newItem = newMap[key]

            val clazz = newItem?.javaClass ?: oldItem!!.javaClass
            for (embeddedField in clazz.declaredFields) {
                val embeddedAnnotation = embeddedField.getAnnotation(Translated::class.java) ?: continue
                embeddedField.isAccessible = true
                val newVal = newItem?.let { embeddedField.get(newItem) }
                val oldVal = oldItem?.let { embeddedField.get(oldItem) }

                if (oldVal != newVal) {
                    fireTranslatedEvent(embeddedAnnotation.event, entity, key, newVal)
                }
            }
        }
    }

    fun getEmbeddedKey(obj: Any): Any {
        return obj::class.java.declaredFields
                .firstOrNull { it.isAnnotationPresent(TranslationKey::class.java) }
                ?.apply { isAccessible = true }
                ?.get(obj)
                ?: throw NoSuchFieldError("No field marked as translation key")
    }

    private fun fireTranslatedEvent(
            eventClass: KClass<out TranslatedFieldChangedEvent<*, *, *>>,
            entity: Any,
            id: Any,
            newValue: Any?
    ) {
        try {
            val constructor = eventClass.java.constructors
                    .find { ctor ->
                        val params = ctor.parameterTypes
                        params.size == 3 &&
                                params[0].isAssignableFrom(entity.javaClass) &&
                                params[1].isAssignableFrom(id.javaClass) &&
                                (newValue == null || params[2].isAssignableFrom(newValue.javaClass))
                    }
                    ?: throw NoSuchMethodException("No suitable constructor found for ${eventClass.simpleName}")
            val event = constructor.newInstance(entity, id, newValue) as TranslatedFieldChangedEvent<*, *, *>
            eventPublisher.publishEvent(event)
        } catch (ex: Exception) {
            throw RuntimeException("Failed to create or publish event: ${eventClass.simpleName}", ex)
        }
    }

    private fun getEntityId(entity: Any): Any? {
        return entity::class.java.declaredFields
                .firstOrNull { it.isAnnotationPresent(Id::class.java) }
                ?.apply { isAccessible = true }
                ?.get(entity)
    }
}