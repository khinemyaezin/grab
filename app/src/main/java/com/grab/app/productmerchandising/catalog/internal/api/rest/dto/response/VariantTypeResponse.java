package com.grab.app.productmerchandising.catalog.internal.api.rest.dto.response;

import java.util.List;

public record VariantTypeResponse(
        List<VariantTypeItem> types
) {
    public record VariantTypeItem(
            String id,
            String name
    ) {
    }
}
