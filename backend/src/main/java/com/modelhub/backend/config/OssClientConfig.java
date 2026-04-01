package com.modelhub.backend.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(OssProperties.class)
public class OssClientConfig {

    @Bean
    public S3Client s3Client(OssProperties ossProperties) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(ossProperties.getRegion()))
                .credentialsProvider(
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(ossProperties.getAccessKey(), ossProperties.getSecretKey())
                        )
                )
                .serviceConfiguration(
                        S3Configuration.builder()
                                .pathStyleAccessEnabled(true)
                                .build()
                );

        if (ossProperties.getEndpoint() != null && !ossProperties.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(ossProperties.getEndpoint()));
        }
        return builder.build();
    }
}

