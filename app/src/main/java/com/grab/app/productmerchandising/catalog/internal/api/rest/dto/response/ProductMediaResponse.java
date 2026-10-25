package com.grab.app.productmerchandising.catalog.internal.api.rest.dto.response;

import com.grab.app.productmerchandising.catalog.api.representation.GetProductResponse;

import java.io.Serializable;
import java.util.List;

public record ProductMediaResponse(
        String productId,
        List<GetProductResponse.Media> medias
) implements Serializable {
}
