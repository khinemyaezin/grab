package com.catalog.domain.service.impl;

import com.grab.framework.id.Id;
import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.domain.valueobject.ProductVariation;
import lombok.AllArgsConstructor;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

@AllArgsConstructor
public class DefaultMatrixKeyGenerator implements MatrixKeyGenerator {
    private final Comparator<ProductVariation> VARIATION_COMPARATOR;

    private static final String KEY_SEPARATOR = "|";
    private static final String KEY_VALUE_SEPARATOR = "=";
    private static final int KEY_LENGTH = 16;

    public String generateKey(List<ProductVariation> variations) {
        List<ProductVariation> sortedVariations = new ArrayList<>(variations);
        sortedVariations.sort(VARIATION_COMPARATOR);

        StringBuilder sb = new StringBuilder();
        for (ProductVariation variation : sortedVariations) {
            appendKeyPart(sb, variation.getTypeId(), variation.getOptionId());
        }
        return hashKey(sb.toString());
    }

    private void appendKeyPart(StringBuilder sb, Id typeId, Id optionId) {
        sb.append(typeId.getValue())
                .append(KEY_VALUE_SEPARATOR)
                .append(optionId.getValue())
                .append(KEY_SEPARATOR);
    }

    private String hashKey(String canonicalKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(canonicalKey.getBytes(StandardCharsets.UTF_8));
            String encoded = Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
            return encoded.substring(0, KEY_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }
}
