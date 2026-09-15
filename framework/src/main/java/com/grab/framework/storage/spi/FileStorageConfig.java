package com.grab.framework.storage.spi;

import java.time.Duration;
import java.util.Map;

/**
 * Backend-agnostic storage settings. The S3 adapter reads well-known keys;
 * other adapters may ignore unused entries.
 */
public record FileStorageConfig(
        String endpoint,
        String region,
        String bucket,
        String accessKey,
        String secretKey,
        String publicBaseUrl,
        Duration presignTtl,
        String keyPrefix,
        Map<String, String> extra
) {
    public FileStorageConfig {
        extra = extra == null ? Map.of() : Map.copyOf(extra);
        presignTtl = presignTtl == null ? Duration.ofMinutes(15) : presignTtl;
        keyPrefix = keyPrefix == null ? "" : keyPrefix;
        region = region == null || region.isBlank() ? "us-east-1" : region;
    }
}
