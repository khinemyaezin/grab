package com.grab.app.identityaccount.identity.internal.api.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank String password) {
}
