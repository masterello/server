package com.masterello.task.converter

import com.masterello.task.dto.TaskStatus
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class TaskStatusConverter : AttributeConverter<TaskStatus, Int> {

    override fun convertToDatabaseColumn(attribute: TaskStatus?): Int? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: Int?): TaskStatus? {
        return dbData?.let { code ->
            TaskStatus.entries.firstOrNull { it.code == code }
        }
    }
}
