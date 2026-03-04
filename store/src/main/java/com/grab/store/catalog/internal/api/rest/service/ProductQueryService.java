package com.grab.store.catalog.internal.api.rest.service;

import com.grab.store.catalog.internal.api.rest.assembler.GetProductBySlugModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.GetProductModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.ProductCombinationModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.ProductSummaryModelAssembler;
import com.grab.store.catalog.internal.api.rest.dto.request.ProductCombinationRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.ProductSummaryRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.GetProductBySlugResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.GetProductResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.ProductCombinationResponse;
import com.grab.store.catalog.internal.api.rest.dto.response.ProductSummaryResponse;
import com.grab.store.catalog.internal.api.rest.mapper.GetProductBySlugDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.GetProductDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.ProductCombinationDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.ProductSummaryDtoMapper;
import com.grab.store.catalog.internal.api.rest.mapper.ProductSummaryQueryMapper;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.store.catalog.internal.query.GetFeaturedProductsQuery;
import com.grab.store.catalog.internal.query.GetProductBySlugQuery;
import com.grab.store.catalog.internal.query.GetProductBySlugResult;
import com.grab.store.catalog.internal.query.GetProductQuery;
import com.grab.store.catalog.internal.query.GetProductResult;
import com.grab.store.catalog.internal.query.GetProductsByCategoryQuery;
import com.grab.store.catalog.internal.query.ProductCombinationQuery;
import com.grab.store.catalog.internal.query.ProductCombinationResult;
import com.grab.store.catalog.internal.query.ProductSummaryQuery;
import com.grab.store.catalog.internal.query.ProductSummaryResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private final QueryBus queryBus;
    private final GetProductDtoMapper getProductDtoMapper;
    private final GetProductBySlugDtoMapper getProductBySlugDtoMapper;
    private final GetProductModelAssembler getProductModelAssembler;
    private final GetProductBySlugModelAssembler getProductBySlugModelAssembler;
    private final ProductCombinationDtoMapper productCombinationDtoMapper;
    private final ProductCombinationModelAssembler productCombinationModelAssembler;
    private final ProductSummaryModelAssembler productSummaryModelAssembler;
    private final ProductSummaryQueryMapper productSummaryQueryMapper;
    private final ProductSummaryDtoMapper productSummaryDtoMapper;

    public EntityModel<GetProductResponse> getProduct(String productId) {
        log.info("Getting product: {}", productId);

        GetProductQuery query = new GetProductQuery(productId);
        GetProductResult result = queryBus.dispatch(query);
        GetProductResponse response = getProductDtoMapper.toResponse(result);
        return getProductModelAssembler.toModel(response);
    }

    public EntityModel<GetProductResponse> getStorefrontProduct(String productId) {
        log.info("Getting storefront product: {}", productId);

        GetProductQuery query = new GetProductQuery(productId);
        GetProductResult result = queryBus.dispatch(query);
        if (!"ACTIVE".equalsIgnoreCase(result.status())) {
            throw new IllegalArgumentException("Product not found: " + productId);
        }

        GetProductResult storefrontResult = new GetProductResult(
                result.id(),
                result.name(),
                result.categoryId(),
                result.status(),
                result.slug(),
                result.featured(),
                result.variants().stream()
                        .filter(variant -> "ACTIVE".equalsIgnoreCase(variant.status()))
                        .toList(),
                result.variantTypes()
        );

        GetProductResponse response = getProductDtoMapper.toResponse(storefrontResult);
        return getProductModelAssembler.toModel(response);
    }

    public EntityModel<ProductCombinationResponse> getProductCombination(ProductCombinationRequest request) {
        log.info("Product combination: {}", request.product().name());

        ProductCombinationQuery query = productCombinationDtoMapper.toQuery(request);
        ProductCombinationResult result = queryBus.dispatch(query);
        ProductCombinationResponse response = productCombinationDtoMapper.toResponse(result);
        return productCombinationModelAssembler.toModel(response);
    }

    public EntityModel<ProductSummaryResponse> getProductSummary(ProductSummaryRequest request) {
        log.info("Searching products");

        ProductSummaryQuery query = productSummaryQueryMapper.toQuery(
                request.productName(),
                request.sku(),
                request.variantStatus(),
                request.variations(),
                request.page(),
                request.size()
        );

        ProductSummaryResult result = queryBus.dispatch(query);
        ProductSummaryResponse response = productSummaryDtoMapper.toResponse(result);
        return productSummaryModelAssembler.toModel(response);
    }

    public EntityModel<ProductSummaryResponse> getProductsByCategory(String categoryId, int page, int size) {
        log.info("Getting products by category: {}", categoryId);

        GetProductsByCategoryQuery query = new GetProductsByCategoryQuery(categoryId, page, size);
        ProductSummaryResult result = queryBus.dispatch(query);
        ProductSummaryResponse response = productSummaryDtoMapper.toResponse(result);

        return productSummaryModelAssembler.toModel(response);
    }

    public EntityModel<GetProductBySlugResponse> getProductBySlug(String slug) {
        log.info("Getting product by slug: {}", slug);

        GetProductBySlugQuery query = new GetProductBySlugQuery(slug);
        GetProductBySlugResult result = queryBus.dispatch(query);
        GetProductBySlugResponse response = getProductBySlugDtoMapper.toResponse(result);

        return getProductBySlugModelAssembler.toModel(response);
    }

    public EntityModel<ProductSummaryResponse> getFeaturedProducts(int page, int size) {
        log.info("Getting featured products");

        GetFeaturedProductsQuery query = new GetFeaturedProductsQuery(page, size);
        ProductSummaryResult result = queryBus.dispatch(query);
        ProductSummaryResponse response = productSummaryDtoMapper.toResponse(result);

        return productSummaryModelAssembler.toModel(response);
    }
}
