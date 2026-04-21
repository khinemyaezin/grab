package com.catalog.domain.valueobject;

import com.grab.framework.id.Id;

public record ProductMetadata(
        String name,
        Id categoryId,
        ListingCondition condition,
        String slug
) {
}
