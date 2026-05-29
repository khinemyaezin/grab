package com.grab.store.catalog.internal.query;

import java.util.List;

public record VariantOptionResult(
        List<VariantOptionItem> options
) {
    public record VariantOptionItem(
            String id,
            String name,
            String typeId,
            String typeName
    ) {
    }
}
