package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response;

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
