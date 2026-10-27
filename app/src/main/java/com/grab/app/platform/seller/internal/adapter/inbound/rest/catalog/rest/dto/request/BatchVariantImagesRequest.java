package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.request;

import java.io.Serializable;
import java.util.List;

public record BatchVariantImagesRequest(
        List<String> mediaIds,
        String thumbnailMediaId
) implements Serializable {
}
