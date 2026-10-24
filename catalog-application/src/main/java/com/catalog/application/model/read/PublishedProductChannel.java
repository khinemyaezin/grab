package com.catalog.application.model.read;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;

public record PublishedProductChannel(Id productId, Id salesChannelId) {
    public PublishedProductChannel(String productId, String salesChannelId) {
        this(new CommonId(productId), new CommonId(salesChannelId));
    }
}
