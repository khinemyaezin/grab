package com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response;

import java.util.List;

public record PriceListRuleResponse(
        String id,
        String attribute,
        List<String> values
) {}
