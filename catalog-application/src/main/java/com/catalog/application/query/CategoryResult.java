package com.catalog.application.query;

public record CategoryResult(
        String id,
        String name,
        String parentId,
        boolean active,
        boolean listingAllowed,
        boolean c2cAllowed
) {
}
