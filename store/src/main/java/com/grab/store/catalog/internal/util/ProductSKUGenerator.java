package com.grab.store.catalog.internal.util;

import com.catalog.domain.service.SkuGenerator;
import com.catalog.domain.valueobject.ProductVariation;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

public class ProductSKUGenerator implements SkuGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public enum Policy {
        RANDOM
    }

    private Policy defaultPolicy = Policy.RANDOM;

    @Override
    public String generate(Context context) {
        return generateByPolicy(context, defaultPolicy);
    }

    private String generateByPolicy(Context context, Policy policy) {
        return switch (policy) {
            case RANDOM -> generateRandomSku();
        };
    }

    /**
     * RANDOM policy: Generate random alphanumeric SKU.
     * Example: "SKU-A7B3C9D2"
     */
    private String generateRandomSku() {
        String randomPart = generateRandomString(8);
        return "SKU-" + randomPart;
    }

    private String abbreviate(String name, int maxLength) {
        if (name == null || name.isEmpty()) {
            return "UNK";
        }
        String cleaned = name.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (cleaned.isEmpty()) {
            return "UNK";
        }
        return cleaned.length() <= maxLength ? cleaned : cleaned.substring(0, maxLength);
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

    public void setDefaultPolicy(Policy policy) {
        this.defaultPolicy = policy;
    }

    public Policy getDefaultPolicy() {
        return defaultPolicy;
    }
}