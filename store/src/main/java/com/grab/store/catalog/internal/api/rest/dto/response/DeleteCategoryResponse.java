package com.grab.store.catalog.internal.api.rest.dto.response;

public record DeleteCategoryResponse(
        String id,
        boolean deleted
) {
}
