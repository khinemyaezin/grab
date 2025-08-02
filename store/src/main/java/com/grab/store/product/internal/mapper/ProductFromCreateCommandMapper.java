package com.grab.store.product.internal.mapper;

import com.grab.store.product.internal.command.CreateProductCommand;
import com.grab.store.product.internal.command.VariationCommand;
import com.product.domain.aggregate.product.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
@AllArgsConstructor
public class ProductFromCreateCommandMapper {
    private final ProductFactory productFactory;
    private final ProductVariantFactory productVariantFactory;
    private final ProductCommandMappingUtil productCommandMappingUtil;

    public Product map(CreateProductCommand command) {
        Product product = productFactory.createProduct(command.id(), command.name(), command.categoryId());
        Stream<VariationCommand> variationCommandStream = command.variants().stream().flatMap(variation -> variation.variations().stream());
        Map<VariationCompositeKey, ProductVariation> variationMap = productCommandMappingUtil.createVariationMap(variationCommandStream);

        // Build product variants
        for (CreateProductCommand.VariantCommand variantEntity : command.variants()) {
            List<ProductVariation> productVariations = productCommandMappingUtil.createVariations(variantEntity.variations(), variationMap);
            ProductVariant variant = productVariantFactory.createVariant(product,variantEntity.id(), variantEntity.sku(), productVariations);
            product.addVariant(variant);
        }
        return product;
    }
}
