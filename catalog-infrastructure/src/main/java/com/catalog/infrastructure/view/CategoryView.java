package com.catalog.infrastructure.view;

public record CategoryView(
        String id,
        String name,
        String parentId,
        boolean active,
        boolean listingAllowed,
        boolean c2cAllowed
) {
}
