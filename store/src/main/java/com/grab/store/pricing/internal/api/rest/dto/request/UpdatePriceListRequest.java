package com.grab.store.pricing.internal.api.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record UpdatePriceListRequest(
        @NotBlank String title,
        String description,
        @NotBlank String status,
        @NotBlank String type,
        Instant startsAt,
        Instant endsAt
) {
}
