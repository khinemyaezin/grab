package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response;

public record DeleteCategoryResponse(
        String id,
        boolean deleted
) {
}
