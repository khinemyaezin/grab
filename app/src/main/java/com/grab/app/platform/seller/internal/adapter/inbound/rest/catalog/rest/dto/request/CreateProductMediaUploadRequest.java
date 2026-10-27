package com.grab.app.platform.seller.internal.adapter.inbound.rest.catalog.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.io.Serializable;

public record CreateProductMediaUploadRequest(
        @NotBlank String filename,
        @NotBlank String contentType,
        @Positive Long sizeBytes
) implements Serializable {
}
