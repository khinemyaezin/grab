package com.grab.store.catalog.internal.query;

import java.util.List;

public record CategoryChildrenResult(
        String parentId,
        List<CategoryResult> children
) {
}
