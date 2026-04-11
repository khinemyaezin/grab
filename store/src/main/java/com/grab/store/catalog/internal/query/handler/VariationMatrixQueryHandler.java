package com.grab.store.catalog.internal.query.handler;

import com.catalog.domain.service.MatrixCombinationService;
import com.catalog.domain.service.MatrixKeyGenerator;
import com.catalog.domain.service.dto.VariantOptionSelection;
import com.catalog.domain.service.dto.VariantTypeSelection;
import com.catalog.domain.valueobject.ProductVariation;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.internal.query.VariationMatrixQuery;
import com.grab.store.catalog.internal.query.VariationMatrixResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VariationMatrixQueryHandler implements QueryHandler<VariationMatrixQuery, VariationMatrixResult> {
    private static final Logger log = Loggers.getLogger(VariationMatrixQueryHandler.class);

    private final MatrixCombinationService matrixCombinationService;
    private final MatrixKeyGenerator matrixKeyGenerator;
    private final IdGenerator idGenerator;

    @Override
    @CatalogReadTransactional
    public VariationMatrixResult handle(VariationMatrixQuery query) {
        log.debug("Handling ProductCombinationQuery for product");

        List<VariationMatrixResult.Variant> previewVariants = calculateMatrixCombination(query);
        return getProductCombinationResult(query.variantTypes(), previewVariants);
    }

    @Override
    public Class<VariationMatrixQuery> getQueryType() {
        return VariationMatrixQuery.class;
    }

    private List<VariationMatrixResult.Variant> calculateMatrixCombination(VariationMatrixQuery query) {
        if(query.variantTypes() == null || query.variantTypes().isEmpty()) {
            return Collections.emptyList();
        }

        List<VariantTypeSelection> variantTypes = convertToVariantTypeSelectionList(query.variantTypes());
        List<List<VariantOptionSelection>> matrixCombination = matrixCombinationService.generateMatrixCombination(variantTypes);

        Map<String, List<VariantOptionSelection>> matrixByKey = buildMatrixKeyMap(matrixCombination);

        Set<String> overrideOptionIds = extractOverrideOptionIds(query.variants());
        Set<String> matrixOptionIds = extractMatrixOptionIds(matrixCombination);
        Set<String> changedOptionIds = calculateSymmetricDifference(overrideOptionIds, matrixOptionIds);

        Map<String, VariationMatrixQuery.Variant> overrideVariantByKey = buildOverrideVariantMap(
                query.variants(), changedOptionIds
        );

        // 5. Build result keys: common keys (intersection) + new matrix keys
        Set<String> resultKeys = buildResultSetKeys(overrideVariantByKey.keySet(), matrixByKey.keySet(), changedOptionIds);

        // 6. Build result variants
        return buildResultVariants(resultKeys, matrixByKey);
    }

    private Map<String, List<VariantOptionSelection>> buildMatrixKeyMap(List<List<VariantOptionSelection>> matrixCombination) {
        return matrixCombination.stream()
                .collect(Collectors.toMap(
                        combination -> matrixKeyGenerator.generateKey(convertToProductVariationList(combination)),
                        Function.identity(),
                        (a, b) -> b,
                        LinkedHashMap::new
                ));
    }

    private Set<String> extractOverrideOptionIds(List<VariationMatrixQuery.Variant> overrideVariants) {
        return overrideVariants.stream()
                .flatMap(v -> v.variations().stream())
                .map(VariationMatrixQuery.Variation::optionId)
                .collect(Collectors.toSet());
    }

    private Set<String> extractMatrixOptionIds(List<List<VariantOptionSelection>> matrixCombination) {
        return matrixCombination.stream()
                .flatMap(List::stream)
                .map(variantOption -> variantOption.valueId().getValue())
                .collect(Collectors.toSet());
    }

    private Set<String> calculateSymmetricDifference(Set<String> setA, Set<String> setB) {
        Set<String> symmetricDiff = new HashSet<>(setA);
        symmetricDiff.addAll(setB);
        
        Set<String> intersection = new HashSet<>(setA);
        intersection.retainAll(setB);
        
        symmetricDiff.removeAll(intersection);
        return symmetricDiff;
    }

    private Map<String, VariationMatrixQuery.Variant> buildOverrideVariantMap(
            List<VariationMatrixQuery.Variant> variants, Set<String> changedOptionIds) {
        
        return variants.stream()
                .collect(Collectors.toMap(
                        v -> buildNormalizedKey(v.variations(), changedOptionIds),
                        Function.identity(),
                        (a, b) -> b,
                        LinkedHashMap::new
                ));
    }

    private String buildNormalizedKey(List<VariationMatrixQuery.Variation> variations, Set<String> changedOptionIds) {
        List<ProductVariation> stableVariations = variations.stream()
                .filter(variation -> !changedOptionIds.contains(variation.optionId()))
                .map(variation -> new ProductVariation(
                        idGenerator.convertIdFrom(variation.optionId()),
                        idGenerator.convertIdFrom(variation.typeId())))
                .toList();
        return matrixKeyGenerator.generateKey(stableVariations);
    }

    /**
     * Builds the complete set of result keys by combining:
     * 1. Common keys (exist in both override variants and matrix)
     * 2. New keys (matrix combinations containing changed options)
     */
    private Set<String> buildResultSetKeys(
            Set<String> overrideKeys, Set<String> matrixKeys, Set<String> changedOptionIds) {
        
        Set<String> resultKeys = new LinkedHashSet<>(overrideKeys);
        resultKeys.retainAll(matrixKeys);

        Set<String> newKeys = matrixKeys.stream()
                .filter(key -> containsChangedOption(key, changedOptionIds))
                .collect(Collectors.toCollection(LinkedHashSet::new));
        
        resultKeys.addAll(newKeys);
        return resultKeys;
    }

    private boolean containsChangedOption(String matrixKey, Set<String> changedOptionIds) {
        for (String optionId : changedOptionIds) {
            if (matrixKey.contains(optionId)) {
                return true;
            }
        }
        return false;
    }

    private List<VariationMatrixResult.Variant> buildResultVariants(
            Set<String> resultKeys, Map<String, List<VariantOptionSelection>> matrixByKey) {
        
        List<VariationMatrixResult.Variant> result = new ArrayList<>(resultKeys.size());
        for (String key : resultKeys) {
            List<VariantOptionSelection> matrix = matrixByKey.get(key);
            List<ProductVariation> productVariations = convertToProductVariationList(matrix);
            result.add(new VariationMatrixResult.Variant(key, convertToResultVariationList(productVariations)));
        }
        return result;
    }

    private List<ProductVariation> convertToProductVariationList(List<VariantOptionSelection> combination) {
        return combination.stream()
                .map(optionSelection -> new ProductVariation(
                        optionSelection.valueId(),
                        optionSelection.typeId()
                )).toList();
    }

    private List<VariantTypeSelection> convertToVariantTypeSelectionList(List<VariationMatrixQuery.VariantType> variantTypes) {
        if (variantTypes == null || variantTypes.isEmpty()) {
            return List.of();
        }
        return variantTypes.stream()
                .map(variantType -> new VariantTypeSelection(
                        idGenerator.convertIdFrom(variantType.typeId()),
                       variantType.options().stream()
                                .map(option -> new VariantOptionSelection(
                                        idGenerator.convertIdFrom(option.optionId()),
                                        idGenerator.convertIdFrom(variantType.typeId())))
                                .toList()
                ))
                .toList();
    }

    private List<VariationMatrixResult.Variation> convertToResultVariationList(List<ProductVariation> variations) {
        List<VariationMatrixResult.Variation> resultVariations = new ArrayList<>(variations.size());
        for (ProductVariation variation : variations) {
            VariationMatrixResult.Variation resultVariation = new VariationMatrixResult.Variation(
                    variation.getOptionId().getValue(),
                    variation.getTypeId().getValue()
            );
            resultVariations.add(resultVariation);
        }
        return resultVariations;
    }


    private VariationMatrixResult getProductCombinationResult(List<VariationMatrixQuery.VariantType> variantTypes,
                                                              List<VariationMatrixResult.Variant> previewVariants) {
        return new VariationMatrixResult(
                previewVariants,
                variantTypes.stream()
                        .map(variantType -> new VariationMatrixResult.VariantType(
                                variantType.typeId(),
                                variantType.options().stream()
                                        .map(option -> new VariationMatrixResult.VariantOption(
                                                option.optionId()
                                        )).toList()
                        )).toList()
        );
    }
}
