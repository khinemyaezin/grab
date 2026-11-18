package com.grab.service.sellerbff.internal.adapter.inbound.rest.dto.response;

import java.util.List;

public record PriceSetResponse(
        String id,
        List<PriceEntryResponse> prices
) {}
