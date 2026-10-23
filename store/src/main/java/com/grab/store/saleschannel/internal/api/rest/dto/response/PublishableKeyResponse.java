package com.grab.store.saleschannel.internal.api.rest.dto.response;

public record PublishableKeyResponse(
        String keyId,
        String salesChannelId,
        String label,
        String displayPrefix,
        String createdAt,
        String revokedAt
) {
}
