package com.masterello.file.converter

import com.masterello.file.dto.FileStatus
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class FileStatusConverter : AttributeConverter<FileStatus, Int> {

    override fun convertToDatabaseColumn(attribute: FileStatus?): Int? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: Int?): FileStatus? {
        return dbData?.let { code ->
            FileStatus.entries.firstOrNull { it.code == code }
        }
    }
}
