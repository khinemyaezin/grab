package com.grab.store.catalog.internal.query;

import java.util.List;

public record VariantTypeResult(
        List<VariantTypeItem> types
) {
    public record VariantTypeItem(
            String id,
            String name
    ) {
    }
}
