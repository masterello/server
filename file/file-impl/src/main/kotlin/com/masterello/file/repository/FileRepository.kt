package com.masterello.file.repository

import com.masterello.file.entity.File
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FileRepository: JpaRepository<File, UUID> {

    fun findAllFilesByUserUuid(userUUID: UUID): List<File>

    fun findFileByUuidAndUserUuid(uuid: UUID, userUUID: UUID) : File?
}