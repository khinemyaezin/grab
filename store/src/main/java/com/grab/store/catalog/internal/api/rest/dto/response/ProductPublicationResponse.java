package com.grab.store.catalog.internal.api.rest.dto.response;

import java.util.List;

public record ProductPublicationResponse(
        String productId,
        List<Publication> publications
) {
    public record Publication(String salesChannelId) {
    }
}
