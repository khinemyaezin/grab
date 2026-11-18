package com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response;

import java.util.List;

public record CategoryNodeResponse(
        String id,
        String name,
        String parentId,
        List<CategoryNodeResponse> children
) {}
