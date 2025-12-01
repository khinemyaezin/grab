package com.product.domain.factory.impl;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.product.domain.aggregate.product.*;
import com.product.domain.factory.ProductFactory;
import com.product.domain.service.SkuGenerator;
import com.product.domain.service.VariantCombination;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
public class ProductFactoryImpl implements ProductFactory {
    private final VariantCombination variantCombination;
    private final IdGenerator idGenerator;
    private final SkuGenerator skuGenerator;

    public void create(Product product, List<VariantType> desiredVariantTypes, List<Id> variantIdsToRemove) {
        VariantInputs inputs = prepareInputs(product, desiredVariantTypes, variantIdsToRemove);
        ExistingVariantState existing = mapExistingVariants(product, inputs);
        Set<Id> keysToKeep = synchronizeVariants(product, inputs, existing);
        removeObsoleteVariants(product, keysToKeep);
    }

    public Product create(ProductSpec spec) {
        Product product = new Product(idGenerator.generateId(),spec.name(), spec.categoryId());
        VariantInputs inputs = prepareInputs(product, spec.desiredVariantTypes(), List.of());
        ExistingVariantState existing = mapExistingVariants(product, inputs);
        synchronizeVariants(product, inputs, existing);
        return product;
    }

    private VariantInputs prepareInputs(Product product, List<VariantType> desiredVariantTypes, List<Id> variantIdsToRemove) {
        Objects.requireNonNull(product, "product is required");
        List<VariantType> variantTypes = desiredVariantTypes != null ? desiredVariantTypes : List.of();

        List<String> variantTypeOrder = variantTypes.stream()
                .map(VariantType::getName)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .sorted(Comparator.comparing(Function.identity(),Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
        Set<String> existingTypeNames = product.getVariants().stream()
                .flatMap(v -> v.getVariations().stream())
                .map(ProductVariation::getTypeName)
                .filter(Objects::nonNull)
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        Set<String> newTypeNames = variantTypeOrder.stream()
                .filter(name -> !existingTypeNames.contains(name))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        Set<Id> removalIds = variantIdsToRemove != null
                ? variantIdsToRemove.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
                : Set.of();

        return new VariantInputs(variantTypes, variantTypeOrder, newTypeNames, removalIds);
    }

    private ExistingVariantState mapExistingVariants(Product product, VariantInputs inputs) {
        Map<String, ProductVariant> existingByKey = new LinkedHashMap<>(); // Order preserved
        Set<String> bannedKeys = new HashSet<>();
        Map<String, Integer> keyFirstIndex = new HashMap<>();

        for (int i = 0; i < product.getVariants().size(); i++) {
            ProductVariant variant = product.getVariants().get(i);
            String key = getVariationKey(inputs.variantTypeOrder(), variant.getVariations(), inputs.newTypeNames());

            if (inputs.removalIds().contains(variant.getId())) {
                bannedKeys.add(key);
                continue;
            }
            existingByKey.putIfAbsent(key, variant);
            keyFirstIndex.putIfAbsent(key, i);
        }

        return new ExistingVariantState(existingByKey, bannedKeys, keyFirstIndex);
    }

    private Set<Id> synchronizeVariants(Product product, VariantInputs inputs, ExistingVariantState existing) {
        List<List<VariantOption>> combinations = variantCombination.generateCombinations(inputs.variantTypes());

        // Track how many times a base key is consumed so adding variant is in combination order
        Map<String, Integer> baseKeyUseCount = new HashMap<>();
        // Housekeeping to track keys to remove variants not contained in combinations
        Set<Id> keysToKeep = new HashSet<>();
        String lastUsedKey = null;

        for (int i = 0; i < combinations.size(); i++) {
            List<VariantOption> combination = combinations.get(i);
            List<ProductVariation> variations = combination.stream()
                    .map(this::toVariation)
                    .toList();
            String key = getVariationKey(inputs.variantTypeOrder(), variations, inputs.newTypeNames());
            if (existing.bannedKeys().contains(key)) continue;

            ProductVariant match = existing.existingByKey().get(key);
            ProductVariant replacement;
            if (match != null) {
                int useCount = baseKeyUseCount.getOrDefault(key, 0);
                if (useCount > 0) {
                    SkuGenerator.Context skuContext = new SkuGenerator.Context(
                            product.getId(),
                            product.getName(),
                            variations,
                            match.getSku(),
                            useCount);
                    String sku = skuGenerator.generate(skuContext);
                    Id newVariantId = idGenerator.generateId();
                    // New variant 
                    replacement = new ProductVariant(newVariantId, product.getId(), sku, variations);
                    keysToKeep.add(newVariantId);
                    if (Objects.equals(key, lastUsedKey)) { // preserve ordering within same base key
                        int baseIndex = existing.keyFirstIndex().getOrDefault(key, product.getVariants().size() - 1);
                        int insertionIndex = Math.min(baseIndex + useCount, product.getVariants().size());
                        product.addVariant(replacement, insertionIndex);
                    } else {
                        product.addVariant(replacement);
                    }

                } else {
                    // Replacement
                    replacement = new ProductVariant(match.getId(), product.getId(), match.getSku(), variations);
                    product.updateVariant(replacement);
                    keysToKeep.add(replacement.getId());
                    int baseIndex = existing.keyFirstIndex().getOrDefault(key, product.getVariants().size() - 1);
                    if(baseIndex != i) {
                        existing.keyFirstIndex().put(key, i);
                    }
                }
                baseKeyUseCount.put(key, useCount + 1);
                lastUsedKey = key;
            } else {
                SkuGenerator.Context skuContext = new SkuGenerator.Context(
                        product.getId(),
                        product.getName(),
                        variations,
                        null,
                        0);
                String sku = skuGenerator.generate(skuContext);
                Id newVariantId = idGenerator.generateId();
                replacement = new ProductVariant(newVariantId, product.getId(), sku, variations);
                product.addVariant(replacement);
                keysToKeep.add(newVariantId);
            }
        }

        return keysToKeep;
    }

    private void removeObsoleteVariants(Product product, Set<Id> keysToKeep) {
        product.getVariants().stream()
                .map(ProductVariant::getId)
                .filter(id -> !keysToKeep.contains(id))
                .toList()
                .forEach(product::removeVariant);
    }

    protected String getVariationKey(List<String> typeOrder, Iterable<ProductVariation> variations, Set<String> ignoredTypes) {
        Map<String, String> map = StreamSupport.stream(variations.spliterator(), false)
                .filter(variation -> variation.getTypeName() != null)
                .collect(Collectors.toMap(
                        variation -> variation.getTypeName().toLowerCase(),
                        variation -> variation.getOptionName().toLowerCase()
                ));

        return typeOrder.stream()
                .filter(type -> !ignoredTypes.contains(type))
                .map(type -> {
                    String option = map.get(type);
                    return option == null ? null : type + "=" + option + "|";
                })
                .filter(Objects::nonNull)
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString();
    }
    
    protected ProductVariation toVariation(VariantOption option) {
        return new ProductVariation(option.getName(), option.getId(), Objects.nonNull(option.getVariantType()) ? option.getVariantType().getName(): null);
    }

    private record VariantInputs(List<VariantType> variantTypes,
                                 List<String> variantTypeOrder,
                                 Set<String> newTypeNames,
                                 Set<Id> removalIds) { }

    private record ExistingVariantState(Map<String, ProductVariant> existingByKey,
                                        Set<String> bannedKeys,
                                        Map<String, Integer> keyFirstIndex) { }
}
