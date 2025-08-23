package com.grab.store.product.internal.mapper;

import com.grab.store.product.internal.command.UpdateProductCommand;
import com.grab.store.product.internal.command.VariationCommand;
import com.product.domain.aggregate.product.*;
import lombok.AllArgsConstructor;
import org.mapstruct.Mapper;
import org.mapstruct.ap.internal.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class ProductFromUpdateCommandMapper {
    private final ProductVariantFactory productVariantFactory;
    private final ProductCommandMappingUtil productCommandMappingUtil;

    public void map(Product product, UpdateProductCommand command) {
        product.setName(command.name());
        product.changeCategory(command.categoryId());

        Map<String, UpdateProductCommand.VariantCommand> inputVariantsMap = new LinkedHashMap<>();
        List<ProductVariant> newVariants = new ArrayList<>();

        Stream<VariationCommand> variationCommandStream = command.variants().stream().flatMap(variation -> variation.variations().stream());
        Map<VariationCompositeKey, ProductVariation> variationMap = productCommandMappingUtil.createVariationMap(variationCommandStream);

        for(UpdateProductCommand.VariantCommand variantCommand : command.variants()) {
            if(Strings.isEmpty(variantCommand.id()))
                newVariants.add(createNewVariant(product,variationMap,variantCommand));
            else
                inputVariantsMap.put(variantCommand.id(), variantCommand);
        }

        // variants in product are immutable
        for (ProductVariant productVariant : product.getVariants()) {
            if (!inputVariantsMap.containsKey(productVariant.getId()))
                product.removeVariant(productVariant.getId());
        }

        newVariants.forEach(product::addVariant);
    }

    public ProductVariant createNewVariant(Product product, Map<VariationCompositeKey, ProductVariation> variationMap, UpdateProductCommand.VariantCommand inputVariant) {
        List<ProductVariation> productVariations = productCommandMappingUtil.createVariations(inputVariant.variations(), variationMap);
        return this.productVariantFactory.createVariant(product,"", inputVariant.sku(), productVariations);
    }

}
