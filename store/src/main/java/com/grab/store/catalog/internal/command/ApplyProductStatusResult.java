package com.grab.store.catalog.internal.command;

public record ApplyProductStatusResult(
        String productId,
        String status
) {
}
