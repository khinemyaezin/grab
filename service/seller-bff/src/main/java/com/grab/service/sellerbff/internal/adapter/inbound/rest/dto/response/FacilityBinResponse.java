package com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response;

public record FacilityBinResponse(
        String id,
        String zoneId,
        String code,
        String name,
        Integer maxCapacity,
        boolean active
) {}
