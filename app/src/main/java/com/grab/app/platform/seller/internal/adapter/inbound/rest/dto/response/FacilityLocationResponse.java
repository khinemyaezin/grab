package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response;

public record FacilityLocationResponse(
        String id,
        String merchantId,
        String code,
        String name,
        String type,
        boolean active
) {}
