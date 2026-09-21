package com.catalog.application.model.write;

public record UpdateProductStatusResult(
        String productId,
        String oldStatus,
        String newStatus
) {}
