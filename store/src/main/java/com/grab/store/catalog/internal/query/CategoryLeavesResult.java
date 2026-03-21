package com.grab.store.catalog.internal.query;

import java.util.List;

public record CategoryLeavesResult(
        List<CategoryResult> leaves
) {
}
