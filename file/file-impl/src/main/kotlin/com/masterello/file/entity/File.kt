package com.masterello.file.entity

import com.masterello.file.converter.FileStatusConverter
import com.masterello.file.converter.FileTypeConverter
import com.masterello.file.dto.FileStatus
import com.masterello.file.dto.FileType
import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.OffsetDateTime
import java.util.*

@Entity
@Table(name = "files", schema = "public")
data class File(
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    val uuid: UUID? = null,

    @Column(name = "userUuid")
    var userUuid: UUID = UUID.randomUUID(),

    @Column(name = "file_name", length = 255)
    var fileName: String = "",

    @Column(name = "is_public")
    var isPublic: Boolean = false,

    @Column(name = "task_uuid")
    var taskUuid: UUID? = null,

    @Convert(converter = FileTypeConverter::class)
    @Column(name = "file_type")
    var fileType: FileType = FileType.AVATAR,

    @Convert(converter = FileStatusConverter::class)
    @Column(name = "file_status")
    var fileStatus: FileStatus = FileStatus.UPLOADING,

    @CreationTimestamp
    @Column(name = "created_date", insertable = false, updatable = false)
    val createdDate: OffsetDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_date", insertable = false, updatable = false)
    val updatedDate: OffsetDateTime? = null
)
