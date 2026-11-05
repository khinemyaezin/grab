package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.time.Instant;

public record CreatePriceListRequest(
        @NotBlank(message = "Title is required")
        String title,
        String description,
        String type,
        Instant startsAt,
        Instant endsAt
) {}
