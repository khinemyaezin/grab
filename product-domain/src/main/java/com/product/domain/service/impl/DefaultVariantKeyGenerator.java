package com.product.domain.service.impl;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.domain.aggregate.product.ProductVariation;
import com.product.domain.aggregate.product.VariantOption;
import com.product.domain.service.VariantKeyGenerator;

import java.util.List;
import java.util.Set;

/**
 * Default implementation of VariantKeyGenerator.
 * Generates keys by concatenating typeId=optionId pairs in a consistent order.
 */
public class DefaultVariantKeyGenerator implements VariantKeyGenerator {

    private static final String KEY_SEPARATOR = "|";
    private static final String KEY_VALUE_SEPARATOR = "=";

    @Override
    public String generateCombinationKey(List<VariantOption> combination, List<Id> typeOrder) {
        StringBuilder sb = new StringBuilder();
        for (Id typeId : typeOrder) {
            for (VariantOption option : combination) {
                if (typeId.equals(option.getVariantType().getId())) {
                    appendKeyPart(sb, typeId, option.getId());
                    break;
                }
            }
        }
        return sb.toString();
    }

    @Override
    public String generateVariantKey(ProductVariant variant, List<Id> typeOrder) {
        StringBuilder sb = new StringBuilder();
        for (Id typeId : typeOrder) {
            for (ProductVariation variation : variant.getVariations()) {
                if (typeId.equals(variation.getTypeId())) {
                    Id optionId = variation.getOptionId();
                    if (optionId != null) {
                        appendKeyPart(sb, typeId, optionId);
                    }
                    break;
                }
            }
        }
        return sb.toString();
    }

    @Override
    public String generateVariationKey(List<Id> typeOrder, Iterable<ProductVariation> variations, Set<Id> ignoredTypes) {
        StringBuilder sb = new StringBuilder();
        for (Id typeId : typeOrder) {
            if (ignoredTypes.contains(typeId)) {
                continue;
            }
            for (ProductVariation variation : variations) {
                if (typeId.equals(variation.getTypeId())) {
                    Id optionId = variation.getOptionId();
                    if (optionId != null) {
                        appendKeyPart(sb, typeId, optionId);
                    }
                    break;
                }
            }
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