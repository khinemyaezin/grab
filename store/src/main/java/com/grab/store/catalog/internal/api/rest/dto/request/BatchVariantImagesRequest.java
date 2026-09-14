package com.grab.store.catalog.internal.api.rest.dto.request;

import java.io.Serializable;
import java.util.List;

public record BatchVariantImagesRequest(
        List<String> mediaIds,
        String thumbnailMediaId
) implements Serializable {
}
