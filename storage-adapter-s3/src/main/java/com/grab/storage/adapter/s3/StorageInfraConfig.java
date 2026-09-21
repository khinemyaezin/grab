package com.grab.storage.adapter.s3;

import com.grab.framework.storage.FileStoragePort;
import com.grab.framework.storage.spi.FileStorageConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class StorageInfraConfig {

    @Bean
    public S3StorageProperties s3StorageProperties(
            @Value("${storage.s3.endpoint:http://localhost:8333}") String endpoint,
            @Value("${storage.s3.region:us-east-1}") String region,
            @Value("${storage.s3.bucket:grab-media}") String bucket,
            @Value("${storage.s3.access-key:s3admin}") String accessKey,
            @Value("${storage.s3.secret-key:s3secret}") String secretKey,
            @Value("${storage.s3.public-base-url:http://localhost:8333/grab-media}") String publicBaseUrl,
            @Value("${storage.s3.presign-ttl:PT15M}") Duration presignTtl,
            @Value("${storage.s3.key-prefix:}") String keyPrefix
    ) {
        return new S3StorageProperties(
                endpoint,
                region,
                bucket,
                accessKey,
                secretKey,
                publicBaseUrl,
                presignTtl,
                keyPrefix
        );
    }

    @Bean
    public FileStoragePort fileStoragePort(S3StorageProperties properties) {
        S3FileStorageProvider provider = new S3FileStorageProvider();
        return provider.createPort(new FileStorageConfig(
                properties.endpoint(),
                properties.region(),
                properties.bucket(),
                properties.accessKey(),
                properties.secretKey(),
                properties.publicBaseUrl(),
                properties.presignTtl(),
                properties.keyPrefix(),
                null
        ));
    }
}
