package com.grab.store.catalog.internal.api.rest.dto.response;

import java.io.Serializable;
import java.util.List;

public record ProductMediaResponse(
        String productId,
        List<GetProductResponse.Media> medias
) implements Serializable {
}
