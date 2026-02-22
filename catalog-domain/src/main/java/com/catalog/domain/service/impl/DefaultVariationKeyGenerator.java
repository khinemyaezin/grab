package com.catalog.domain.service.impl;

import com.grab.framework.id.Id;
import com.catalog.domain.service.VariationKeyGenerator;
import com.catalog.domain.valueobject.ProductVariation;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@AllArgsConstructor
public class DefaultVariationKeyGenerator implements VariationKeyGenerator {
    private final Comparator<ProductVariation> VARIATION_COMPARATOR;

    private static final String KEY_SEPARATOR = "|";
    private static final String KEY_VALUE_SEPARATOR = "=";

    public String generateVariationKey(List<ProductVariation> variations) {
        List<ProductVariation> sortedVariations = new ArrayList<>(variations);
        sortedVariations.sort(VARIATION_COMPARATOR);

        StringBuilder sb = new StringBuilder();
        for (ProductVariation variation : sortedVariations) {
            appendKeyPart(sb, variation.getTypeId(), variation.getOptionId());
        }
        return sb.toString();
    }

    private void appendKeyPart(StringBuilder sb, Id typeId, Id optionId) {
        sb.append(typeId.getValue())
                .append(KEY_VALUE_SEPARATOR)
                .append(optionId.getValue())
                .append(KEY_SEPARATOR);
    }
}
