package com.identity.domain.valueobject;

import java.util.Objects;

public record HashedPassword(String hash) {
    public HashedPassword {
        if (Objects.requireNonNull(hash, "password hash is required").isBlank()) {
            throw new IllegalArgumentException("password hash is required");
        }
    }
}
