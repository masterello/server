package com.masterello.file.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import java.net.URI

@Configuration
open class DigitalOceanConfiguration(
    @Value("\${DO_SPACES_ACCESS_KEY}") private val accessKey: String,
    @Value("\${DO_SPACES_SECRET_KEY}") private val secretKey: String,
    @Value("\${DO_SPACES_ENDPOINT}") private val endpoint: String
) {
    @Bean
    open fun s3Client(): S3Client {
        val credentials = AwsBasicCredentials.create(accessKey, secretKey)
        val s3Config = S3Configuration.builder()
            .pathStyleAccessEnabled(true)
            .build()

        return S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .endpointOverride(URI.create(endpoint))
            .serviceConfiguration(s3Config)
            .build()
    }
}