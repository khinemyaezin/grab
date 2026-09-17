package com.grab.store.catalog.queries;

public record StorefrontCategoryChild(
        String id,
        String name,
        String parentId,
        boolean active,
        boolean listingAllowed,
        boolean c2cAllowed
) {
}
