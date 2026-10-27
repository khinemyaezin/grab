package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.response;

import java.io.Serializable;
import java.util.List;

public record ProductMediaResponse(
        String productId,
        List<GetProductResponse.Media> medias
) implements Serializable {
}
