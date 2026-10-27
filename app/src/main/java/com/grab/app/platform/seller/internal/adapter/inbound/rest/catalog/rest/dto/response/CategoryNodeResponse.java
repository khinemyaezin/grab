package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response;

import java.util.List;

public record CategoryNodeResponse(
        String id,
        String name,
        String parentId,
        List<CategoryNodeResponse> children
) {
}
