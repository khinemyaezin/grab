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

    public ProductVariantSynchronizer(VariantCombination variantCombination, IdGenerator idGenerator,
                              SkuGenerator skuGenerator, VariantDeletionStrategy deletionStrategy) {
        this.variantCombination = variantCombination;
        this.idGenerator = idGenerator;
        this.skuGenerator = skuGenerator;
        this.deletionStrategy = deletionStrategy;
    }

    public void synchronize(Product product, List<VariantType> desiredTypes) {
        List<VariantType> filteredTypes = deletionStrategy.filterVariantTypes(product, desiredTypes);
        VariantInputs inputs = prepareInputs(product, filteredTypes);
        List<List<VariantOption>> combinations = variantCombination.generateCombinations(inputs.variantTypes());
        Set<Id> effectedIds = synchronizeVariants(product, inputs, combinations);

        Map<String, Integer> combinationOrder = buildCombinationOrderMap(combinations, inputs);
        sortVariantsByCombinationOrder(product, inputs, combinationOrder);
        deletionStrategy.removeObsoleteVariants(product, effectedIds);
    }

    private VariantInputs prepareInputs(Product product, List<VariantType> desiredVariantTypes) {
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

        Map<String, ProductVariant> existingByKey = new LinkedHashMap<>();

        for (ProductVariant variant : product.getVariants()) {
            String key = getVariationKey(variantTypeOrder, variant.getVariations(), newType);
            existingByKey.putIfAbsent(key, variant);
        }


        return new VariantInputs(variantTypes, variantTypeOrder, newType, existingByKey);
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

    private Map<String, ProductVariant>  mapExistingVariants(Product product, VariantInputs inputs) {
        Map<String, ProductVariant> existingByKey = new LinkedHashMap<>();

        for (ProductVariant variant : product.getVariants()) {
            String key = getVariationKey(inputs.variantTypeOrder(), variant.getVariations(), inputs.newType());
            existingByKey.putIfAbsent(key, variant);
        }

        return existingByKey;
    }

    private Map<String, Integer> buildCombinationOrderMap(List<List<VariantOption>> combinations, VariantInputs inputs) {
        Map<String, Integer> orderMap = new HashMap<>();
        for (int i = 0; i < combinations.size(); i++) {
            List<VariantOption> combination = combinations.get(i);
            String key = getCombinationKey(combination, inputs.variantTypeOrder());
            orderMap.putIfAbsent(key, i);
        }
        return orderMap;
    }

    private String getCombinationKey(List<VariantOption> combination, List<Id> typeOrder) {
        StringBuilder sb = new StringBuilder();
        for (Id typeId : typeOrder) {
            for (VariantOption option : combination) {
                if (typeId.equals(option.getVariantType().getId())) {
                    sb.append(typeId.getValue())
                      .append("=")
                      .append(option.getId().getValue())
                      .append("|");
                    break;
                }
            }
        }
        return sb.toString();
    }

    private String getVariantKey(ProductVariant variant, List<Id> typeOrder) {
        StringBuilder sb = new StringBuilder();
        for (Id typeId : typeOrder) {
            for (ProductVariation variation : variant.getVariations()) {
                if (typeId.equals(variation.getTypeId())) {
                    Id optionId = variation.getOptionId();
                    if (optionId != null) {
                        sb.append(typeId.getValue())
                          .append("=")
                          .append(optionId.getValue())
                          .append("|");
                    }
                    break;
                }
            }
        }
        return sb.toString();
    }

    private void sortVariantsByCombinationOrder(Product product,
                                                 VariantInputs inputs,
                                                 Map<String, Integer> combinationOrder) {
        product.sortVariants((v1, v2) -> {
            String key1 = getVariantKey(v1, inputs.variantTypeOrder());
            String key2 = getVariantKey(v2, inputs.variantTypeOrder());

            int order1 = combinationOrder.getOrDefault(key1, Integer.MAX_VALUE);
            int order2 = combinationOrder.getOrDefault(key2, Integer.MAX_VALUE);

            return Integer.compare(order1, order2);
        });
    }

    private Set<Id> synchronizeVariants(Product product, VariantInputs inputs,
                                         List<List<VariantOption>> combinations) {
        Map<String, Integer> baseKeyUseCount = new HashMap<>();
        Set<Id> effectedIds = new HashSet<>();

        for (List<VariantOption> combination : combinations) {
            List<ProductVariation> variations = combination.stream()
                    .map(this::toVariation)
                    .toList();
            String key = getVariationKey(inputs.variantTypeOrder(), variations, inputs.newType());

            ProductVariant match = inputs.existingVariantByKey().get(key);
            int useCount = baseKeyUseCount.getOrDefault(key, 0);

            if (match != null && useCount == 0) {
                // Update existing variant
                ProductVariant updated = new ProductVariant(
                        match.getId(), product.getId(), match.getSku(),
                        match.getStatus(), variations);
                product.updateVariant(updated);
                effectedIds.add(updated.getId());
            } else {
                String baseSku = match != null ? match.getSku() : null;
                String sku = generateSku(product, variations, baseSku, useCount);
                Id newId = idGenerator.generateId();
                ProductVariant newVariant = new ProductVariant(newId, product.getId(), sku, variations);
                product.addVariant(newVariant);
                effectedIds.add(newId);
            }

            baseKeyUseCount.put(key, useCount + 1);
        }

        return effectedIds;
    }

    private String generateSku(Product product,
                               List<ProductVariation> variations,
                               String baseSku,
                               int useCount) {
        SkuGenerator.Context ctx = new SkuGenerator.Context(
                product.getId(),
                product.getName(),
                variations,
                baseSku,
                useCount,
                Map.of()
        );
        return skuGenerator.generate(ctx);
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

    private record VariantInputs(List<VariantType> variantTypes,
                                 List<Id> variantTypeOrder,
                                 Set<Id> newType,
                                 Map<String, ProductVariant>  existingVariantByKey) { }
}
