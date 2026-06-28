package com.grab.store.merchant.internal.api.rest.dto.request;

import com.merchant.domain.enums.MerchantType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

public record StartMerchantApplicationRequest(
        @NotNull MerchantType type,
        @NotBlank @Size(max = 255) String displayName
) {
}
