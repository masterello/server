package com.masterello.file.converter

import com.masterello.file.dto.FileType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = true)
class FileTypeConverter : AttributeConverter<FileType, Int> {

    override fun convertToDatabaseColumn(attribute: FileType?): Int? {
        return attribute?.code
    }

    override fun convertToEntityAttribute(dbData: Int?): FileType? {
        return dbData?.let { code ->
            FileType.entries.firstOrNull { it.code == code }
        }
    }
}
