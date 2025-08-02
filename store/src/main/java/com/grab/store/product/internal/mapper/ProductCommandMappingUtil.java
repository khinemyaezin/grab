package com.grab.store.product.internal.mapper;

import com.grab.store.product.internal.command.VariationCommand;
import com.product.domain.aggregate.product.ProductVariation;
import com.product.domain.aggregate.product.VariantOption;
import com.product.domain.aggregate.product.VariantType;
import com.product.domain.aggregate.product.VariationCompositeKey;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class ProductCommandMappingUtil {

    public Map<VariationCompositeKey, ProductVariation> createVariationMap(Stream<VariationCommand> variations) {
        final Map<VariantType, VariantType> variantTypes = new HashMap<>();

        // Build variationMap directly
        return variations.collect(Collectors.toMap(
                option -> new VariationCompositeKey(
                        option.optionName(),
                        option.typeName()
                ),
                option -> {
                    // just a key
                    VariantType variantType = new VariantType(
                            option.typeId(),
                            option.typeName()
                    );

                    // if key variantType is new, variantTypeInMap has the same reference with key variantType.
                    // if it is already existed, they have different reference
                    VariantType variantTypeInMap = variantTypes.computeIfAbsent(variantType, k -> variantType);
                    VariantOption variantOption = new VariantOption(
                            option.optionId(),
                            option.optionName(),
                            variantTypeInMap
                    );
                    variantTypeInMap.addOption(variantOption);
                    return new ProductVariation(variantOption);
                },
                (existing, replacement) -> existing // Handle duplicate keys
        ));
    }

    public List<ProductVariation> createVariations(List<VariationCommand> variationCommands, Map<VariationCompositeKey, ProductVariation> variationMap) {
        return variationCommands.stream()
                .map(variation -> variationMap.get(
                        new VariationCompositeKey(
                                variation.optionName(),
                                variation.typeName()
                        )
                )).toList();
    }
}
