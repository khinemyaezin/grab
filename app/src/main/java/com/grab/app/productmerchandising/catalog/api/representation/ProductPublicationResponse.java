package com.grab.app.productmerchandising.catalog.api.representation;

import java.util.List;

public record ProductPublicationResponse(
        String productId,
        List<Publication> publications
) {
    public record Publication(String variantId, String salesChannelId) {
    }
}
