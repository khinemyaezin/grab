package com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response;

public record SellerUserProfileResponse(
        String id,
        String email,
        String status,
        String createdAt
) {}

