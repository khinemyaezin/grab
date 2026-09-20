package com.catalog.domain.service.impl;

import com.catalog.domain.service.SkuGenerator;

import java.security.SecureRandom;

public class DefaultSkuGenerator implements SkuGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    @Override
    public String generate(Context context) {
        return "SKU-" + generateRandomString(8);
    }

    private String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUMERIC.charAt(RANDOM.nextInt(ALPHANUMERIC.length())));
        }
        return sb.toString();
    }
}
