package com.grab.app.platform.seller.internal.adapter.inbound.rest.merchant.rest.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ChangeMerchantMemberRoleRequest(
        @NotBlank String role
) {
}
