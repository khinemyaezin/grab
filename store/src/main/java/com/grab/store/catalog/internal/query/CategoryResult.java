package com.grab.store.catalog.internal.query;

public record CategoryResult(
        String id,
        String name,
        String parentId,
        boolean active,
        boolean listingAllowed,
        boolean reviewRequired,
        boolean c2cAllowed
) {
}
