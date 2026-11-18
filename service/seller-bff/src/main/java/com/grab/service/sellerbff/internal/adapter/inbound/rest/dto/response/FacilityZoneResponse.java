package com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response;

public record FacilityZoneResponse(
        String id,
        String locationId,
        String code,
        String name,
        String type,
        boolean active
) {}
