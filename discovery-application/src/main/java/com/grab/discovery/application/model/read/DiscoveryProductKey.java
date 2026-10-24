package com.grab.discovery.application.model.read;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;

public record DiscoveryProductKey(Id productId, Id salesChannelId) {
    public DiscoveryProductKey(String productId, String salesChannelId) {
        this(new CommonId(productId), new CommonId(salesChannelId));
    }
}
