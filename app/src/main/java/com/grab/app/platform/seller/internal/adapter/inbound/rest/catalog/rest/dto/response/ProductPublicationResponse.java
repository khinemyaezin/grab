package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response;

import java.util.List;

public record ProductPublicationResponse(
        String productId,
        List<Publication> publications
) {
    public record Publication(String variantId, String salesChannelId) {
    }
}
