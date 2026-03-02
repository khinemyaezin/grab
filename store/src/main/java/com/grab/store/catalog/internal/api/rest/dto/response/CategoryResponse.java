package com.grab.store.catalog.internal.api.rest.dto.response;

public record CategoryResponse(
        String id,
        String name,
        String parentId
) {
}
