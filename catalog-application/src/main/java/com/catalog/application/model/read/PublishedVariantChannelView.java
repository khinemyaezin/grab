package com.catalog.application.model.read;

import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;

public record PublishedVariantChannelView(Id variantId, Id salesChannelId) {
    public PublishedVariantChannelView(String variantId, String salesChannelId) {
        this(new CommonId(variantId), new CommonId(salesChannelId));
    }
}
