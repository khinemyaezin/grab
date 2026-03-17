package com.grab.store.catalog.internal.command;

import com.grab.framework.id.Id;

import java.util.List;

public record GetProductPayload(
        String sellerId,
        String sellerType,
        String condition,
        boolean offerEligible,
        List<Description> descriptions,
        List<Media> medias,
        String moderationNote
) {
    public record Description(
            Id id,
            String name,
            String title,
            String description
    ) {}

    public record Media(
            Id id,
            String type,
            String path
    ) {}
}
