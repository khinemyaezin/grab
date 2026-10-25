package com.grab.app.productmerchandising.catalog.internal.api.rest.dto.response;

import java.util.List;

public record CategoryChildrenResponse(
        String parentId,
        List<CategoryResponse> children
) {
}
