package com.grab.app.channeldiscovery.saleschannel.internal.api.rest.dto.response;

public record PublishableKeyResponse(
        String keyId,
        String salesChannelId,
        String label,
        String displayPrefix,
        String createdAt,
        String revokedAt
) {
}
