package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response;

import java.util.List;

public record CategoryChildrenResponse(
        String parentId,
        List<CategoryResponse> children
) {
}
