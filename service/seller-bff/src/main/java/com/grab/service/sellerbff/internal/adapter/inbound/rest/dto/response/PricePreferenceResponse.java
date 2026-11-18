package com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response;

public record PricePreferenceResponse(
        String id,
        String attribute,
        String value,
        boolean taxInclusive
) {}
