package com.catalog.application.query;

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
