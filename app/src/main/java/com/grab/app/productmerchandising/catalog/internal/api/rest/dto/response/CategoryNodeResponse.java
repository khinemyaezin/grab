package com.grab.app.productmerchandising.catalog.internal.api.rest.dto.response;

import java.util.List;

public record CategoryNodeResponse(
        String id,
        String name,
        String parentId,
        List<CategoryNodeResponse> children
) {
}
