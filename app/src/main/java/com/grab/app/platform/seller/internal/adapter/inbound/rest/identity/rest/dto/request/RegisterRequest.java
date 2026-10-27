package com.grab.app.platform.seller.internal.adapter.inbound.rest.identity.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank String password) {
}
