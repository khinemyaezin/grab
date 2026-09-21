package com.catalog.application.model.read;

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
