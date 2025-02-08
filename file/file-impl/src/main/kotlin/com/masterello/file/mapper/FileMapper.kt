package com.masterello.file.mapper

import com.masterello.file.dto.FileDto
import com.masterello.file.dto.FileStatus
import com.masterello.file.dto.FileType
import com.masterello.file.entity.File
import org.springframework.stereotype.Service

@Service
class  FileMapper {

    fun mapFileDtoToFile(dto: FileDto?, type: FileType, status: FileStatus): File {
        if (dto == null) {
            throw IllegalArgumentException("FileDto cannot be null")
        }

        return File(
            userUuid = dto.userUuid,
            fileType = type,
            fileName = dto.fileName,
            isPublic = dto.isPublic,
            taskUuid = dto.taskUuid,
            fileStatus = status
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
            taskUuid = file.taskUuid,
            fileStatus = file.fileStatus
        )
    }
}