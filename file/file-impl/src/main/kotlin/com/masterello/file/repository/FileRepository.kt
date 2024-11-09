package com.masterello.file.repository

import com.masterello.file.entity.File
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface FileRepository: JpaRepository<File, UUID> {

    fun findAllFilesByUserUuid(userUUID: UUID): List<File>

    @Query(nativeQuery = true, value = """
        SELECT * from files f
        WHERE f.file_type IN (0,1) and f.user_uuid = :userUuid
    """)
    fun findAllImagesByUserUuid(@Param("userUuid") userUUID: UUID): List<File>

    @Query(nativeQuery = true, value = """
        SELECT * from files f
        WHERE f.file_type = 1 and f.user_uuid = :userUuid
    """)
    fun findAllThumbnailsByUserUuid(@Param("userUuid") userUUID: UUID): List<File>

    fun findFileByUuidAndUserUuid(uuid: UUID, userUUID: UUID) : File?
}