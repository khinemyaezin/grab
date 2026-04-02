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
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class VariationMatrixQueryHandler implements QueryHandler<VariationMatrixQuery, VariationMatrixResult> {

    private static final Logger log = Loggers.getLogger(VariationMatrixQueryHandler.class);

    private final VariantCombinationService variantCombinationService;
    private final VariationKeyGenerator variationKeyGenerator;
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
        List<List<VariantOptionSelection>> combinations = variantCombinationService.generateCombinations(variantTypes);
        List<VariationMatrixResult.Variant> result = new ArrayList<>(combinations.size());
        Map<String, VariationMatrixQuery.Variant> variantByMatrixKey = variantByMatrixKeyInOrder(query.variants());

        for(List<VariantOptionSelection> combination : combinations) {
            List<ProductVariation> productVariations = convertToProductVariationList(combination);
            String key = variationKeyGenerator.generateVariationKey(productVariations);

            VariationMatrixQuery.Variant variant = variantByMatrixKey.get(key);
            if(Objects.isNull(variant)) {
                VariationMatrixResult.Variant newVariant = new VariationMatrixResult.Variant(
                        key,
                        convertToResultVariationList(productVariations)
                );
                result.add(newVariant);
            }
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

    private Map<String, VariationMatrixQuery.Variant> variantByMatrixKeyInOrder(List<VariationMatrixQuery.Variant> variants) {
        return variants.stream()
                .collect(Collectors.toMap(
                        VariationMatrixQuery.Variant::matrixKey,
                        v -> v,
                        (existing, replacement) -> existing, // 3. Merge Function: Keeps the first one found
                        LinkedHashMap::new
                ));
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
