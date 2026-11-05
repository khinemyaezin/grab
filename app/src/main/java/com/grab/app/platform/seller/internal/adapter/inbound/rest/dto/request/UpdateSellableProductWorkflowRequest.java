package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

public record UpdateSellableProductWorkflowRequest(
        @NotBlank(message = "Title is required")
        String title,
        String description,
        String categoryId,
        List<String> publishSalesChannelIds,
        List<String> unpublishSalesChannelIds,
        Map<String, String> attributes
) {}

