package com.product.domain.service.impl;

import com.grab.framework.id.Id;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.domain.valueobject.ProductVariation;
import com.product.domain.aggregate.product.VariantType;
import com.product.domain.service.VariantInputsFactory;
import com.product.domain.service.VariantKeyGenerator;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class DefaultVariantInputsFactory implements VariantInputsFactory {

    private final VariantKeyGenerator keyGenerator;

    @Override
    public VariantInputs prepare(Product product, List<VariantType> desiredVariantTypes) {
        Objects.requireNonNull(product, "product is required");
        List<VariantType> variantTypes = desiredVariantTypes != null ? desiredVariantTypes : List.of();

        Set<Id> existingTypes = collectExistingTypeIds(product);

        List<Id> variantTypeOrder = new ArrayList<>();
        Set<Id> newType = new LinkedHashSet<>();

        Set<Id> seen = new HashSet<>();
        for (VariantType vt : variantTypes) {
            Id id = vt.getId();
            if (id != null && seen.add(id)) {
                variantTypeOrder.add(id);
                if (!existingTypes.contains(id)) {
                    newType.add(id);
                }
            }
        }

        variantTypeOrder.sort(Comparator.comparing(
                Id::getValue, Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER)));

        if (!newType.isEmpty() && variantTypeOrder.size() > 1) {
            Set<Id> sortedNewType = new LinkedHashSet<>();
            for (Id id : variantTypeOrder) {
                if (newType.contains(id)) {
                    sortedNewType.add(id);
                }
            }
            newType = sortedNewType;
        }

        Map<String, ProductVariant> existingVariantByKey = mapExistingVariants(product, variantTypeOrder, newType);

        return new VariantInputs(variantTypeOrder, newType, existingVariantByKey);
    }

    private Set<Id> collectExistingTypeIds(Product product) {
        Set<Id> existingTypes = new HashSet<>();
        for (ProductVariant variant : product.getVariants()) {
            for (ProductVariation variation : variant.getVariations()) {
                Id typeId = variation.getTypeId();
                if (typeId != null) {
                    existingTypes.add(typeId);
                }
            }
        }
        return existingTypes;
    }

    private Map<String, ProductVariant> mapExistingVariants(Product product,
                                                             List<Id> variantTypeOrder,
                                                             Set<Id> newTypes) {
        Map<String, ProductVariant> existingByKey = new LinkedHashMap<>();

        for (ProductVariant variant : product.getVariants()) {
            String key = keyGenerator.generateVariationKey(variantTypeOrder, variant.getVariations(), newTypes);
            existingByKey.putIfAbsent(key, variant);
        }

        return existingByKey;
    }
}