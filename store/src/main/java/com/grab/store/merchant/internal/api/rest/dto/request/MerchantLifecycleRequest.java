package com.grab.store.merchant.internal.api.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

public record MerchantLifecycleRequest(
        @NotBlank @Size(max = 1000) String reason,
        @Null(message = "actorId must not be supplied") String actorId
) {
}
