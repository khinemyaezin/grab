package com.catalog.application.model.write;

import java.time.Instant;
import java.util.Map;

public record ProductMediaUploadResult(
        String url,
        String method,
        Map<String, String> requiredHeaders,
        String storageKey,
        Instant expiresAt
) {
}
