package com.grab.store.product.internal.query.handler;

import com.grab.framework.id.IdGenerator;
import com.grab.store.product.internal.cqrs.query.QueryHandler;
import com.grab.store.product.internal.query.GetProductQuery;
import com.grab.store.product.internal.query.GetProductResult;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.domain.repository.ProductRepository;
import com.product.domain.valueobject.ProductVariation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetProductQueryHandler implements QueryHandler<GetProductQuery, GetProductResult> {

    private final ProductRepository productRepository;
    private final IdGenerator idGenerator;

    @Override
    public GetProductResult handle(GetProductQuery query) {
        log.debug("Handling GetProductQuery for productId: {}", query.productId());

        Product product = productRepository.find(idGenerator.generateId(query.productId()))
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + query.productId()));

        return mapToResult(product);
    }

    @Override
    public Class<GetProductQuery> getQueryType() {
        return GetProductQuery.class;
    }

    private GetProductResult mapToResult(Product product) {
        List<GetProductResult.Variant> variants = product.getVariants().stream()
                .map(this::mapToResultVariant)
                .toList();

        List<GetProductResult.VariantType> variantTypes = extractVariantTypes(product);

        return new GetProductResult(
                product.getId().getValue(),
                product.getName(),
                product.getCategoryId().getValue(),
                variants,
                variantTypes
        );
    }

    private List<GetProductResult.VariantType> extractVariantTypes(Product product) {
        Map<String, List<GetProductResult.VariantOption>> typeOptionsMap = new LinkedHashMap<>();
        Map<String, String> typeNameMap = new LinkedHashMap<>();

        for (ProductVariant variant : product.getVariants()) {
            for (ProductVariation variation : variant.getVariations()) {
                String typeId = variation.getTypeId().getValue();
                typeNameMap.putIfAbsent(typeId, variation.getTypeName());

                typeOptionsMap.computeIfAbsent(typeId, k -> new ArrayList<>());
                List<GetProductResult.VariantOption> options = typeOptionsMap.get(typeId);

                boolean exists = options.stream()
                        .anyMatch(o -> o.optionId().equals(variation.getOptionId().getValue()));
                if (!exists) {
                    options.add(new GetProductResult.VariantOption(
                            variation.getOptionId().getValue(),
                            variation.getOptionName()
                    ));
                }
            }
        }

        return typeOptionsMap.entrySet().stream()
                .map(entry -> new GetProductResult.VariantType(
                        entry.getKey(),
                        typeNameMap.get(entry.getKey()),
                        entry.getValue()
                ))
                .toList();
    }

    private GetProductResult.Variant mapToResultVariant(ProductVariant variant) {
        List<GetProductResult.Variation> variations = variant.getVariations().stream()
                .map(this::mapToResultVariation)
                .toList();

        return new GetProductResult.Variant(
                variant.getId().getValue(),
                variant.getSku(),
                variant.getStatus().name(),
                variations
        );
    }

    private GetProductResult.Variation mapToResultVariation(ProductVariation variation) {
        return new GetProductResult.Variation(
                variation.getOptionId().getValue(),
                variation.getOptionName(),
                variation.getTypeId().getValue(),
                variation.getTypeName()
        );
    }
}
