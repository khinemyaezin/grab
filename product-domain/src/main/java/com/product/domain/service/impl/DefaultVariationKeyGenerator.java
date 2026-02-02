package com.product.domain.service.impl;

import com.grab.framework.id.Id;
import com.product.domain.service.VariationKeyGenerator;
import com.product.domain.valueobject.ProductVariation;

import java.util.List;

public class DefaultVariationKeyGenerator implements VariationKeyGenerator {
    private static final String KEY_SEPARATOR = "|";
    private static final String KEY_VALUE_SEPARATOR = "=";

    public String generateVariationKey(List<ProductVariation> variations) {
        StringBuilder sb = new StringBuilder();
        for (ProductVariation variation : variations) {
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
