package com.masterello.file.configuration

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "masterello.file")
data class FileProperties @ConstructorBinding constructor(
    val maxWidth: Int,
    val maxHeight: Int,
    val compressedSize: Int,
    val bucketName: String,
    val cdnLink: String
)
