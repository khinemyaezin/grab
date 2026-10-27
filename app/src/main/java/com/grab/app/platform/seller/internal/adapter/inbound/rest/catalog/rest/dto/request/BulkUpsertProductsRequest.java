package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.io.Serializable;
import java.util.List;

public record BulkUpsertProductsRequest(
        @Valid @NotEmpty List<SaveProductRequest> products
) implements Serializable {
}
