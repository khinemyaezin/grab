package com.grab.storage.adapter.s3;

import java.time.Duration;

public record S3StorageProperties(
        String endpoint,
        String region,
        String bucket,
        String accessKey,
        String secretKey,
        String publicBaseUrl,
        Duration presignTtl,
        String keyPrefix
) {
    public S3StorageProperties {
        if (bucket == null || bucket.isBlank()) {
            throw new IllegalArgumentException("storage.s3.bucket is required");
        }
        region = region == null || region.isBlank() ? "us-east-1" : region;
        presignTtl = presignTtl == null ? Duration.ofMinutes(15) : presignTtl;
        keyPrefix = keyPrefix == null ? "" : keyPrefix;
        publicBaseUrl = publicBaseUrl == null || publicBaseUrl.isBlank()
                ? defaultPublicBaseUrl(endpoint, bucket)
                : publicBaseUrl;
    }

    private static String defaultPublicBaseUrl(String endpoint, String bucket) {
        if (endpoint == null || endpoint.isBlank()) {
            return "/" + bucket;
        }
        String trimmed = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        return trimmed + "/" + bucket;
    }
}
