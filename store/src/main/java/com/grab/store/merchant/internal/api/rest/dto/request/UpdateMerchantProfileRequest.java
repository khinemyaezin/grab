package com.grab.store.merchant.internal.api.rest.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

public record UpdateMerchantProfileRequest(
        @NotBlank @Size(max = 255) String legalName,
        @NotBlank @Size(max = 255) String displayName,
        @Size(min = 2, max = 2) String registrationCountryCode,
        @Size(max = 255) String registrationNumber,
        @NotBlank @Email @Size(max = 255) String contactEmail,
        @NotBlank @Size(max = 64) String contactPhone,
        @NotBlank @Size(max = 255) String addressLine1,
        @Size(max = 255) String addressLine2,
        @NotBlank @Size(max = 128) String addressCity,
        @Size(max = 128) String addressRegion,
        @NotBlank @Size(max = 32) String addressPostalCode,
        @NotBlank @Size(min = 2, max = 2) String addressCountryCode
) {
}
