package com.masterello.file.mapper

import com.masterello.file.dto.FileDto
import com.masterello.file.dto.FileType
import com.masterello.file.entity.File
import org.springframework.stereotype.Service
import java.util.*

@Service
class  FileMapper {

    fun mapFileDtoToFile(dto: FileDto?, type: FileType, fileName: String, fileExtension: String): File {
        if (dto == null) {
            throw IllegalArgumentException("FileDto cannot be null")
        }

        return File(
            userUuid = dto.userUuid,
            fileType = type,
            fileName = fileName,
            isPublic = dto.isPublic,
            fileExtension = fileExtension
        )
    }

    fun mapAvatarThumbnailToFile(dto: FileDto?, type: FileType, fileName: String, fileExtension: String,
                                 size: Int?, parentImage: UUID, taskUuid: UUID?): File {
        if (dto == null) {
            throw IllegalArgumentException("FileDto cannot be null")
        }

        return File(
            userUuid = dto.userUuid,
            fileType = type,
            fileName = fileName,
            isPublic = dto.isPublic,
            parentImage = parentImage,
            taskUuid = taskUuid,
            thumbnailSize = size,
            fileExtension = fileExtension
        )
    }

    fun mapFileToDto(file: File): FileDto  {
        return FileDto(
            uuid = file.uuid,
            userUuid = file.userUuid,
            fileType = file.fileType,
            fileName = file.fileName,
            isPublic = file.isPublic,
            createdDate = file.createdDate,
            updatedDate = file.updatedDate,
            file = null
        )
    }
}