package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response;

public record SellerUserProfileResponse(
        String id,
        String email,
        String status,
        String createdAt
) {}

