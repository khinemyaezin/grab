package com.grab.store.product.internal.query.handler;

import com.grab.store.product.internal.cqrs.query.QueryHandler;
import com.grab.store.product.internal.query.GetAllProductsQuery;
import com.grab.store.product.internal.query.GetAllProductsResult;
import com.product.domain.aggregate.product.Product;
import com.product.domain.aggregate.product.ProductVariant;
import com.product.domain.valueobject.ProductVariation;
import com.product.domain.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetAllProductsQueryHandler implements QueryHandler<GetAllProductsQuery, GetAllProductsResult> {

    private final ProductRepository productRepository;

    @Override
    public GetAllProductsResult handle(GetAllProductsQuery query) {
        log.debug("Handling GetAllProductsQuery");

      /*  List<Product> products = productRepository.findAll();

        List<GetAllProductsResult.Product> resultProducts = products.stream()
                .map(this::mapToResultProduct)
                .toList();*/

        //log.info("Retrieved {} products", resultProducts.size());

        return new GetAllProductsResult(null);
    }

    @Override
    public Class<GetAllProductsQuery> getQueryType() {
        return GetAllProductsQuery.class;
    }

    private GetAllProductsResult.Product mapToResultProduct(Product product) {
        List<GetAllProductsResult.Variant> variants = product.getVariants().stream()
                .map(this::mapToResultVariant)
                .toList();

        return new GetAllProductsResult.Product(
                product.getId().getValue(),
                product.getName(),
                product.getCategoryId().getValue(),
                variants
        );
    }

    private GetAllProductsResult.Variant mapToResultVariant(ProductVariant variant) {
        List<GetAllProductsResult.Variation> variations = variant.getVariations().stream()
                .map(this::mapToResultVariation)
                .toList();

        return new GetAllProductsResult.Variant(
                variant.getId().getValue(),
                variant.getSku(),
                variant.getStatus().name(),
                variations
        );
    }

    private GetAllProductsResult.Variation mapToResultVariation(ProductVariation variation) {
        return new GetAllProductsResult.Variation(
                variation.getOptionName(),
                variation.getOptionId().getValue(),
                variation.getTypeId().getValue(),
                variation.getTypeName()
        );
    }
}
