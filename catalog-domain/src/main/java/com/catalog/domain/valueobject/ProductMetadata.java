package com.catalog.domain.valueobject;

import com.grab.framework.id.Id;

public record ProductMetadata(
        String name,
        Id categoryId,
        Id sellerId,
        SellerType sellerType,
        ListingCondition condition,
        boolean offerEligible,
        boolean featured,
        String slug,
        String moderationNote
) {
}
