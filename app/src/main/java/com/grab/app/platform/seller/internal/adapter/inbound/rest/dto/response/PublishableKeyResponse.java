package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response;

import java.time.Instant;

public record PublishableKeyResponse(
        String keyId,
        String salesChannelId,
        String label,
        String rawToken,
        String displayPrefix,
        Instant createdAt
) {}

