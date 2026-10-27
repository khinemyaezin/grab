package com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.dto.response;

public record PublishableKeyResponse(
        String keyId,
        String salesChannelId,
        String label,
        String displayPrefix,
        String createdAt,
        String revokedAt
) {
}
