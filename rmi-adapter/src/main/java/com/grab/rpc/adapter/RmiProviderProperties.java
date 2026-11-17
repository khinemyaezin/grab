package com.grab.rpc.adapter;

import java.util.Objects;

public record RmiProviderProperties(
        int registryPort,
        String bindAddress,
        String advertisedHost,
        int connectTimeoutMillis,
        int readTimeoutMillis) {

    public RmiProviderProperties {
        if (registryPort < 1 || registryPort > 65_535) {
            throw new IllegalArgumentException("RMI registry port must be between 1 and 65535");
        }
        bindAddress = requireText(bindAddress, "bindAddress");
        advertisedHost = requireText(advertisedHost, "advertisedHost");
        if (connectTimeoutMillis < 1) {
            throw new IllegalArgumentException("RMI connect timeout must be positive");
        }
        if (readTimeoutMillis < 1) {
            throw new IllegalArgumentException("RMI read timeout must be positive");
        }
    }

    private static String requireText(String value, String name) {
        String normalizedValue = Objects.requireNonNull(value, name).trim();
        if (normalizedValue.isEmpty()) {
            throw new IllegalArgumentException("RMI " + name + " must not be blank");
        }
        return normalizedValue;
    }
}
