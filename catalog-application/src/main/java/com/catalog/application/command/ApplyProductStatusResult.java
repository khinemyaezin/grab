package com.catalog.application.command;

public record ApplyProductStatusResult(
        String productId,
        String status
) {
}
