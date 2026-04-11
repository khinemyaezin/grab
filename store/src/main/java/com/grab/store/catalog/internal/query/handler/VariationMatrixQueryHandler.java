package com.grab.store.catalog.internal.query.handler;

import com.catalog.domain.service.*;
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
        Map<String, List<VariantOptionSelection>> matrixCombinationByKey = getMatrixCombinationByKey(matrixCombination);

        Set<String> overrideOptionIdSet = getOverrideOptionIdSet(query.variants());
        Set<String> matrixOptionIdSet = getMatrixOptionIdSet(matrixCombination);

        Set<String> newOptionKeyList = calculatePositiveDelta(overrideOptionIdSet, matrixOptionIdSet);

        Map<String, VariationMatrixQuery.Variant> normalizedOverrideVariantMap = diffOptions(newOptionKeyList, query.variants());

        Set<String> newMatrixCombinationKeys = getNewMatrixKeys(newOptionKeyList, matrixCombinationByKey.keySet());

        Set<String> commonKeySet = getCommonMatrixKeys(normalizedOverrideVariantMap.keySet(), matrixCombinationByKey.keySet());

        Set<String> resultMatrixSet = new LinkedHashSet<>(commonKeySet);
        resultMatrixSet.addAll(newMatrixCombinationKeys);

        List<VariationMatrixResult.Variant> result = new ArrayList<>(resultMatrixSet.size());
        for(String key : resultMatrixSet) {
            List<VariantOptionSelection> matrix = matrixCombinationByKey.get(key);
            List<ProductVariation> productVariations = convertToProductVariationList(matrix);

            VariationMatrixQuery.Variant overrideVariant = normalizedOverrideVariantMap.get(key);
            VariationMatrixResult.Variant newVariant;
            if(overrideVariant != null) newVariant = new VariationMatrixResult.Variant(key, convertToResultVariationList(productVariations));
            else newVariant = new VariationMatrixResult.Variant(key, convertToResultVariationList(productVariations));

            result.add(newVariant);
        }
        return result;
    }

    private Map<String, List<VariantOptionSelection>> getMatrixCombinationByKey(List<List<VariantOptionSelection>> matrixCombination) {
        return matrixCombination.stream()
                .collect(Collectors.toMap(
                        combination -> matrixKeyGenerator.generateKey(
                                combination.stream()
                                        .map(variantOption -> new ProductVariation(
                                                variantOption.valueId(),
                                                variantOption.typeId()
                                        )).toList()
                        ),
                        Function.identity(),
                        (a,b) -> b,
                        LinkedHashMap::new
                ));
    }

    private Set<String> getOverrideOptionIdSet(List<VariationMatrixQuery.Variant> overrideVariants){
        return overrideVariants.stream()
                .flatMap( v-> v.variations().stream())
                .map(VariationMatrixQuery.Variation::optionId)
                .collect(Collectors.toSet());
    }

    private Set<String> getMatrixOptionIdSet(List<List<VariantOptionSelection>> matrixCombination){
        return matrixCombination.stream()
                .flatMap(List::stream)
                .map(variantOption -> variantOption.valueId().getValue())
                .collect(Collectors.toSet());
    }

    private Set<String> calculatePositiveDelta(Set<String> overrideOptionIdSet, Set<String> matrixOptionIdSet){
        Set<String> diff = new HashSet<>(overrideOptionIdSet); // Copy A
        Set<String> tempB = new HashSet<>(matrixOptionIdSet); // Copy B

        diff.removeAll(matrixOptionIdSet); // diff now has {y}
        tempB.removeAll(overrideOptionIdSet); // tempB now has {x}

        diff.addAll(tempB);
        return diff;
    }

    private Map<String, VariationMatrixQuery.Variant> diffOptions(Set<String> newOptionKeyList, List<VariationMatrixQuery.Variant> matrixOptionIdSet){
        return matrixOptionIdSet.stream()
                .collect( Collectors.toMap(
                        v -> {
                                List<ProductVariation> productVariations = v.variations().stream()
                                        .filter( variation -> !newOptionKeyList.contains(variation.optionId()))
                                        .map(variation -> new ProductVariation(
                                                idGenerator.convertIdFrom(variation.optionId()),
                                                idGenerator.convertIdFrom(variation.typeId())))
                                        .toList();
                                return matrixKeyGenerator.generateKey(productVariations);
                        },
                        Function.identity(),
                        (a,b)-> b,
                        LinkedHashMap::new
                ));
    }

    private Set<String> getNewMatrixKeys(Set<String> newOptionKeyList, Set<String> matrixCombinationByKeySet){
        return  matrixCombinationByKeySet.stream()
                .filter(k -> newOptionKeyList.stream().anyMatch(k::contains))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> getCommonMatrixKeys(Set<String> normalizedOverrideVariantMapKeySet, Set<String> matrixCombinationByKeyEntrySet){
        Set<String> commonKeySet = new LinkedHashSet<>(normalizedOverrideVariantMapKeySet);
        commonKeySet.retainAll(matrixCombinationByKeyEntrySet);
        return commonKeySet;
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
