package com.product.domain.service.impl;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.product.domain.aggregate.product.*;
import com.product.domain.service.SkuGenerator;
import com.product.domain.service.VariantCombination;
import com.product.domain.service.VariantDeletionStrategy;
import lombok.RequiredArgsConstructor;

import java.util.*;

@RequiredArgsConstructor
public class ProductVariantSynchronizer {
    private final IdGenerator idGenerator;
    private final SkuGenerator skuGenerator;
    private final VariantCombination variantCombination;
    private final VariantDeletionStrategy deletionStrategy;

    public ProductVariantSynchronizer(VariantCombination variantCombination, IdGenerator idGenerator, SkuGenerator skuGenerator) {
        this(variantCombination, idGenerator, skuGenerator, new DefaultDeletionStrategy());
    }

    public ProductVariantSynchronizer(VariantCombination variantCombination, IdGenerator idGenerator,
                              SkuGenerator skuGenerator, VariantDeletionStrategy deletionStrategy) {
        this.variantCombination = variantCombination;
        this.idGenerator = idGenerator;
        this.skuGenerator = skuGenerator;
        this.deletionStrategy = deletionStrategy;
    }

    public void synchronize(Product product, List<VariantType> desiredTypes) {
        List<VariantType> filteredTypes = deletionStrategy.filterVariantTypes(product, desiredTypes);
        VariantInputs inputs = prepareInputs(product, filteredTypes, List.of());
        ExistingVariantState existing = mapExistingVariants(product, inputs);
        Set<Id> keysToKeep = synchronizeVariants(product, inputs, existing);
        deletionStrategy.removeObsoleteVariants(product, keysToKeep);
    }

    private VariantInputs prepareInputs(Product product, List<VariantType> desiredVariantTypes, List<Id> variantIdsToRemove) {
        Objects.requireNonNull(product, "product is required");
        List<VariantType> variantTypes = desiredVariantTypes != null ? desiredVariantTypes : List.of();
        Set<Id> existingTypes = collectExistingTypeIds(product);

        List<Id> variantTypeOrder = new ArrayList<>();
        Set<Id> newType = new LinkedHashSet<>();

        Set<Id> seen = new HashSet<>();
        for (VariantType vt : variantTypes) {
            Id id = vt.getId();
            if (id != null && seen.add(id)) {  // add returns false if already present (handles distinct)
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

        Set<Id> removalIds = collectRemovalIds(variantIdsToRemove);

        return new VariantInputs(variantTypes, variantTypeOrder, newType);
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


    private Set<Id> collectRemovalIds(List<Id> variantIdsToRemove) {
        if (variantIdsToRemove == null || variantIdsToRemove.isEmpty()) {
            return Set.of();
        }
        Set<Id> removalIds = new HashSet<>(variantIdsToRemove.size());
        for (Id id : variantIdsToRemove) {
            if (id != null) {
                removalIds.add(id);
            }
        }
        return removalIds;
    }


    private ExistingVariantState mapExistingVariants(Product product, VariantInputs inputs) {
        Map<String, ProductVariant> existingByKey = new LinkedHashMap<>(); // Order preserved
        Map<String, Integer> keyFirstIndex = new HashMap<>();

        for (int i = 0; i < product.getVariants().size(); i++) {
            ProductVariant variant = product.getVariants().get(i);
            String key = getVariationKey(inputs.variantTypeOrder(), variant.getVariations(), inputs.newType());

            existingByKey.putIfAbsent(key, variant);
            keyFirstIndex.putIfAbsent(key, i);
        }

        return new ExistingVariantState(existingByKey, keyFirstIndex);
    }

    private Set<Id> synchronizeVariants(Product product, VariantInputs inputs, ExistingVariantState existing) {
        List<List<VariantOption>> combinations = variantCombination.generateCombinations(inputs.variantType());

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
            String key = getVariationKey(inputs.variantTypeOrder(), variations, inputs.newType());

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
                            useCount,
                            Map.of());
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
                    replacement = new ProductVariant(match.getId(), product.getId(), match.getSku(), match.getStatus(), variations);
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
                        0,
                        Map.of());
                String sku = skuGenerator.generate(skuContext);
                Id newVariantId = idGenerator.generateId();
                replacement = new ProductVariant(newVariantId, product.getId(), sku, variations);
                product.addVariant(replacement);
                keysToKeep.add(newVariantId);
            }
        }

        return keysToKeep;
    }

    protected String getVariationKey(List<Id> typeOrder, Iterable<ProductVariation> variations, Set<Id> ignoredTypes) {
        StringBuilder sb = new StringBuilder();

        for (Id type : typeOrder) {
            if (ignoredTypes.contains(type)) continue;

            // Variations size can only reach maximum 6, negligible O of n lookup
            for (ProductVariation variation : variations) {
                if (type.equals(variation.getTypeId())) {
                    Id option = variation.getOptionId();
                    if (option != null) {
                        sb.append(type.getValue()).append("=").append(option.getValue()).append("|");
                    }
                    break;
                }
            }
        }
        return sb.toString();
    }

    protected ProductVariation toVariation(VariantOption option) {
        return new ProductVariation(option.getName(),
                option.getId(),
                option.getVariantType().getName(),
                option.getVariantType().getId());
    }

    private record VariantInputs(List<VariantType> variantType,
                                 List<Id> variantTypeOrder,
                                 Set<Id> newType) { }

    private record ExistingVariantState(Map<String, ProductVariant> existingByKey,
                                        Map<String, Integer> keyFirstIndex) { }

}
