package com.masterello.file.repository

import com.masterello.file.entity.File
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface FileRepository: JpaRepository<File, UUID> {

    fun findAllFilesByUserUuid(userUUID: UUID): List<File>

    @Query(nativeQuery = true, value = """
        SELECT * from files f
        WHERE f.file_type IN (0,2) and f.user_uuid = :userUuid
        AND f.file_status = 1
    """)
    fun findAllImagesByUserUuid(@Param("userUuid") userUUID: UUID): List<File>

    @Query(nativeQuery = true, value = """
        SELECT * FROM files f
        WHERE f.file_type = 0
        AND f.user_uuid = :userUuid
    """)
    fun getAvatarByUserUuid(@Param("userUuid") userUUID: UUID): Optional<File>

    @Query(nativeQuery = true, value = """
        SELECT * FROM files f
        WHERE f.file_type = :fileType
        AND f.user_uuid IN(:userUuids)
        AND f.file_status = 1
    """)
    fun findAllImagesByUserUuidsAndType(@Param("fileType") fileType: Int,
                                        @Param("userUuids") userUUID: List<UUID>): List<File>

    fun findFileByUuidAndUserUuid(uuid: UUID, userUUID: UUID) : File?

    @Query(nativeQuery = true, value = """
        SELECT * from files f
        WHERE f.user_uuid = :userUuid and uuid in (:fileIds)
    """)
    fun findAllFilesByIdsAndUserUuid(@Param("userUuid") userUUID: UUID,
                                     @Param("fileIds") fileIds: List<UUID>): List<File>

    @Query(nativeQuery = true, value = """
        SELECT * from files f
        WHERE f.file_status = 0 and f.created_date < now() - '1 day'::interval
    """)
    fun findNotUploadedImages(): List<File>
}