package com.grab.storage.adapter.s3;

import com.grab.framework.storage.FileStoragePort;
import com.grab.framework.storage.spi.FileStorageConfig;
import com.grab.framework.storage.spi.FileStorageProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.net.URI;

public class S3FileStorageProvider implements FileStorageProvider {

    public static final String ID = "s3";

    @Override
    public String id() {
        return ID;
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public FileStoragePort createPort(FileStorageConfig config) {
        S3StorageProperties properties = new S3StorageProperties(
                config.endpoint(),
                config.region(),
                config.bucket(),
                config.accessKey(),
                config.secretKey(),
                config.publicBaseUrl(),
                config.presignTtl(),
                config.keyPrefix()
        );
        S3Client client = s3Client(properties);
        S3Presigner presigner = s3Presigner(properties);
        return new S3FileStorageAdapter(client, presigner, properties);
    }

    static S3Client s3Client(S3StorageProperties properties) {
        var builder = S3Client.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(credentials(properties))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build());
        if (properties.endpoint() != null && !properties.endpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.endpoint()));
        }
        return builder.build();
    }

    static S3Presigner s3Presigner(S3StorageProperties properties) {
        var builder = S3Presigner.builder()
                .region(Region.of(properties.region()))
                .credentialsProvider(credentials(properties))
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build());
        if (properties.endpoint() != null && !properties.endpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.endpoint()));
        }
        return builder.build();
    }

    private static StaticCredentialsProvider credentials(S3StorageProperties properties) {
        return StaticCredentialsProvider.create(
                AwsBasicCredentials.create(properties.accessKey(), properties.secretKey())
        );
    }
}
