package com.grab.store.catalog.internal.service;

import com.catalog.domain.service.SkuGenerator;
import lombok.Getter;
import lombok.Setter;

import java.security.SecureRandom;

@Setter
@Getter
public class ProductSKUGenerator implements SkuGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    public enum Policy {
        RANDOM
    }

    private Policy defaultPolicy = Policy.RANDOM;

    @Override
    public String generate(Context context) {
        return generateByPolicy(defaultPolicy);
    }

    private String generateByPolicy(Policy policy) {
        return switch (policy) {
            case RANDOM -> generateRandomSku();
        };
    }

    private String generateRandomSku() {
        String randomPart = generateRandomString(8);
        return "SKU-" + randomPart;
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }

}