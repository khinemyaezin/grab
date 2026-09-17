package com.grab.store.storefront.internal.api.rest.dto.response;

import java.util.List;

public record StorefrontCategoryNodeResponse(
        String id,
        String name,
        String parentId,
        List<StorefrontCategoryNodeResponse> children
) {
}
