package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response;

public record FacilityZoneResponse(
        String id,
        String locationId,
        String code,
        String name,
        String type,
        boolean active
) {}
