package com.grab.store.merchant.internal.api.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeMerchantMemberRoleRequest(
        @NotBlank String role
) {
}
