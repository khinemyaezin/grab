package com.grab.store.catalog.internal.api.rest.dto.response;

import java.util.List;

public record VariantOptionResponse(
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
