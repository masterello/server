package com.masterello.file.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3Configuration
import java.net.URI

@Configuration
open class DigitalOceanConfiguration(
    private val digitalOceanProperties: DigitalOceanProperties
) {
    @Bean
    open fun s3Client(): S3Client {
        val credentials = AwsBasicCredentials.create(digitalOceanProperties.accessKey, digitalOceanProperties.secretKey)
        val s3Config = S3Configuration.builder()
            .pathStyleAccessEnabled(true)
            .build()

        return S3Client.builder()
            .credentialsProvider(StaticCredentialsProvider.create(credentials))
            .endpointOverride(URI.create(digitalOceanProperties.endpoint))
            .serviceConfiguration(s3Config)
            .region(Region.of(digitalOceanProperties.region))
            .build()
    }
}