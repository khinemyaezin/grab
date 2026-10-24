package com.grab.discovery.application.model.read;

import com.grab.framework.id.Id;

public record ShoppingContext(
        Id salesChannelId,
        Id regionId,
        String countryCode,
        String currencyCode,
        String locale
) {
}
