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
        SELECT * FROM files f
        WHERE f.file_type = 0
        AND f.user_uuid = :userUuid
        OR f.parent_image IN (
            SELECT f2.uuid 
            FROM files f2 
            WHERE f2.file_type = 0
            AND f2.user_uuid = :userUuid
        )
    """)
    fun getAllIAvatarsByUserUuid(@Param("userUuid") userUUID: UUID): List<File>

    @Query(nativeQuery = true, value = """
        SELECT * from files f
        WHERE (f.file_type = 1 AND f.parent_image = :parentUuid) OR f.uuid = :parentUuid
    """)
    fun getAllThumbnailsByParentImageUuid(@Param("parentUuid") parentUuid: UUID): List<File>

    @Query(nativeQuery = true, value = """
        SELECT * FROM files f
        WHERE f.file_type = :fileType
        AND f.user_uuid IN(:userUuids)
        OR f.parent_image IN (
            SELECT f2.uuid 
            FROM files f2 
            WHERE f2.file_type = :fileType
            AND f2.user_uuid IN(:userUuids))
    """)
    fun findAllImagesByUserUuidsAndType(@Param("fileType") fileType: Int,
                                        @Param("userUuids") userUUID: List<UUID>): List<File>

    @Query(nativeQuery = true, value = """
        SELECT * from files f
        WHERE f.file_type = 1 and f.user_uuid = :userUuid
    """)
    fun findAllThumbnailsByUserUuid(@Param("userUuid") userUUID: UUID): List<File>

    fun findFileByUuidAndUserUuid(uuid: UUID, userUUID: UUID) : File?
}