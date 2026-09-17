package com.grab.store.catalog.queries;

import java.util.List;

public record StorefrontCategoryNode(
        String id,
        String name,
        String parentId,
        List<StorefrontCategoryNode> children
) {
}
