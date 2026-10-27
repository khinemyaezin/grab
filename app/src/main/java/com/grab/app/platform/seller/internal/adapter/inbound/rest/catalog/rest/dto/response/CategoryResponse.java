package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response;

public record CategoryResponse(
        String id,
        String name,
        String parentId,
        boolean active,
        boolean listingAllowed,
        boolean c2cAllowed
) {
}
