package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

public record UpdateProductRequest(
        String name,
        String categoryId,
        String condition,
        String slug
) {}
