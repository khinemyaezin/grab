package com.grab.framework.storage;

import java.time.Instant;
import java.util.Map;

public record PresignedUpload(
        String url,
        String method,
        Map<String, String> requiredHeaders,
        String storageKey,
        Instant expiresAt
) {
    public PresignedUpload {
        method = method == null ? "PUT" : method;
        requiredHeaders = requiredHeaders == null ? Map.of() : Map.copyOf(requiredHeaders);
    }
}
