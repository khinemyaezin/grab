package com.grab.app.platform.seller.internal.adapter.inbound.rest.pricing.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

public record CreatePriceListRequest(
        @NotBlank String title,
        String description,
        String type,
        Instant startsAt,
        Instant endsAt
) {
}
