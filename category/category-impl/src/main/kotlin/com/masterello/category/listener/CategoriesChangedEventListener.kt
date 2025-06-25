package com.masterello.category.listener

import com.masterello.category.event.CategoriesChangedEvent
import com.masterello.commons.async.MasterelloEventListener
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.cache.annotation.CacheEvict
import org.springframework.stereotype.Component

@Component
class CategoriesChangedEventListener : MasterelloEventListener<CategoriesChangedEvent>() {

    private val log = KotlinLogging.logger {}

    @CacheEvict("getAllCategories", allEntries = true)
    override fun onApplicationEvent(event: CategoriesChangedEvent) {
        super.onApplicationEvent(event)
    }

    override fun processEvent(event: CategoriesChangedEvent?) {
        log.info { "Evict getAllCategories cache" }
    }
}