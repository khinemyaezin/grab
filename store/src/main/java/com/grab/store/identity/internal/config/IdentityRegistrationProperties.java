package com.grab.store.identity.internal.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.Locale;

@Validated
@ConfigurationProperties("identity.registration")
public record IdentityRegistrationProperties(
        @NotBlank @Pattern(regexp = "[A-Z][A-Z0-9_]*") String platformCode,
        @NotBlank @Pattern(regexp = "[A-Z][A-Z0-9_]*") String roleCode
) {
    public IdentityRegistrationProperties {
        platformCode = normalize(platformCode);
        roleCode = normalize(roleCode);
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim().toUpperCase(Locale.ROOT);
    }
}
