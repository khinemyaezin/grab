package com.grab.store.storefront.internal.api.rest.dto.response;

import java.util.List;

public record StorefrontCategoryChildrenResponse(
        String parentId,
        List<StorefrontCategoryChildResponse> children
) {
    public record StorefrontCategoryChildResponse(
            String id,
            String name,
            String parentId,
            boolean active,
            boolean listingAllowed,
            boolean c2cAllowed
    ) {
    }
}
