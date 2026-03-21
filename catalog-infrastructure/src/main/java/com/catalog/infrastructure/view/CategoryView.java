package com.catalog.infrastructure.view;

public record CategoryView(
        String id,
        String name,
        String parentId,
        boolean active,
        boolean listingAllowed,
        boolean reviewRequired,
        boolean c2cAllowed
) {
}
