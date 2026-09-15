package com.grab.store.catalog.internal.api.rest.dto.response;

import java.time.Instant;
import java.util.Map;

public record ProductMediaUploadResponse(
        String url,
        String method,
        Map<String, String> requiredHeaders,
        String storageKey,
        Instant expiresAt
) {
}
