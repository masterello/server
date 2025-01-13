package com.masterello.task.converter

import com.masterello.task.dto.ReviewerType
import com.masterello.task.dto.TaskStatus
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class TaskReviewerTypeConverter : AttributeConverter<ReviewerType, Int> {

    override fun convertToDatabaseColumn(attribute: ReviewerType?): Int? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: Int?): ReviewerType? {
        return dbData?.let { code ->
            ReviewerType.entries.firstOrNull { it.code == code }
        }
    }
}
