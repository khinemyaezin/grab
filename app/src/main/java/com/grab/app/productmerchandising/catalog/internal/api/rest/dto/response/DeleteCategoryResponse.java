package com.grab.app.productmerchandising.catalog.internal.api.rest.dto.response;

public record DeleteCategoryResponse(
        String id,
        boolean deleted
) {
}
