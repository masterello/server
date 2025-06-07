package com.masterello.category.event

import org.springframework.context.ApplicationEvent

class CategoriesChangedEvent(source: Any) : ApplicationEvent(source)
