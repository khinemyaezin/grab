package com.grab.store.merchant.internal.api.rest.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StartMerchantApplicationRequest(
        @NotBlank @Size(max = 255) String displayName
) {
}
