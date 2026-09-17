package com.grab.store.catalog.queries;

import java.util.List;

public record StorefrontCategoryChildren(
        String parentId,
        List<StorefrontCategoryChild> children
) {
}
