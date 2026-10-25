package com.grab.app.productmerchandising.catalog.internal.api.rest.dto.response;

import java.util.List;

public record CategoryLeavesResponse(
        List<CategoryResponse> leaves
) {
}
