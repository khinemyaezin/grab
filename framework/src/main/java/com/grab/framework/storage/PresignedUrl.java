package com.grab.framework.storage;

import java.time.Instant;

public record PresignedUrl(
        String url,
        Instant expiresAt
) {
}
