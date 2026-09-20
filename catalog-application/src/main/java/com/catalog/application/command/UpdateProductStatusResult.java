package com.catalog.application.command;

public record UpdateProductStatusResult(
        String productId,
        String oldStatus,
        String newStatus
) {}
