package com.grab.app.platform.seller.internal.adapter.inbound.rest.saleschannel.rest.dto.response;

public record CreatedPublishableKeyResponse(
        String keyId,
        String salesChannelId,
        String label,
        String rawToken,
        String displayPrefix,
        String createdAt
) {
    @Override
    public String toString() {
        return "CreatedPublishableKeyResponse[keyId=" + keyId + ", rawToken=[REDACTED]]";
    }
}
