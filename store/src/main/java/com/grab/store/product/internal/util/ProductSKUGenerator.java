package com.grab.store.product.internal.util;

import com.product.domain.valueobject.ProductVariation;
import com.product.domain.service.SkuGenerator;

import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Collectors;

public class ProductSKUGenerator implements SkuGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public enum Policy {
        RANDOM,
        HYBRID
    }

    private Policy defaultPolicy = Policy.HYBRID;

    @Override
    public String generate(Context context) {
        return generateByPolicy(context, defaultPolicy);
    }

    private String generateByPolicy(Context context, Policy policy) {
        return switch (policy) {
            case RANDOM -> generateRandomSku();
            case HYBRID -> generateHybridSku(context);
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

    /**
     * HYBRID policy: Combines product name and variation abbreviations.
     * Example: Product="T-Shirt", Color=Red, Size=L → "TSH-RED-L"
     */
    private String generateHybridSku(Context context) {
        String productAbbr = abbreviate(context.productName(), 3);
        String variationsStr = generateFromVariations(context.orderedVariations());

        String baseSku;
        if (variationsStr.isEmpty()) {
            baseSku = productAbbr;
        } else {
            baseSku = productAbbr + "-" + variationsStr;
        }

        return baseSku;
    }

    private String generateFromVariations(List<ProductVariation> variations) {
        if (variations == null || variations.isEmpty()) {
            return "";
        }

        return variations.stream()
                .map(v -> abbreviate(v.getOptionName(), 3))
                .collect(Collectors.joining("-"));
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