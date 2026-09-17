package com.grab.store.storefront.internal.api.rest.dto.request;

public record StorefrontProductSearchRequest(
        String query,
        String categoryId,
        String condition,
        Boolean featured
) {
}
