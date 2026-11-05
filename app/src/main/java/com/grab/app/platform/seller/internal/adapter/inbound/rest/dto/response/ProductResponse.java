package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response;

import java.util.List;

public record ProductResponse(
        String id,
        String name,
        String categoryId,
        String categoryName,
        String condition,
        String status,
        String slug,
        List<VariantResponse> variants
) {}
