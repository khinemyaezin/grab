package com.grab.store.catalog.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.api.rest.assembler.GetProductBySlugModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.GetProductModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.ProductCombinationModelAssembler;
import com.grab.store.catalog.internal.api.rest.assembler.ProductSummaryModelAssembler;
import com.grab.store.catalog.internal.api.rest.dto.request.VariationMatrixRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.ProductSummaryRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.*;
import com.grab.store.catalog.internal.api.rest.mapper.*;
import com.grab.store.catalog.internal.exception.CatalogServiceError;
import com.grab.store.catalog.internal.exception.CatalogServiceException;
import com.grab.store.catalog.internal.query.*;
import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.EntityModel;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private static final Logger log = Loggers.getLogger(ProductQueryService.class);

    private final QueryBus queryBus;
    private final GetProductDtoMapper getProductDtoMapper;
    private final GetProductBySlugDtoMapper getProductBySlugDtoMapper;
    private final GetProductModelAssembler getProductModelAssembler;
    private final GetProductBySlugModelAssembler getProductBySlugModelAssembler;
    private final VariationMatrixQueryMapper variationMatrixQueryMapper;
    private final ProductCombinationModelAssembler productCombinationModelAssembler;
    private final ProductSummaryModelAssembler productSummaryModelAssembler;
    private final ProductSummaryDtoMapper productSummaryDtoMapper;
    private final ProductAuditDtoMapper productAuditDtoMapper;

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
        GetProductResponse response = getProductDtoMapper.toResponse(result);
        return getProductModelAssembler.toModel(response);
    }

    public EntityModel<VariationMatrixResponse> getMatrixCombination(VariationMatrixRequest request) {
        log.info("Product combination");

        VariationMatrixQuery query = variationMatrixQueryMapper.toQuery(request);
        VariationMatrixResult result = queryBus.dispatch(query);
        VariationMatrixResponse response = variationMatrixQueryMapper.toResponse(result);
        return productCombinationModelAssembler.toModel(response);
    }

    public EntityModel<ProductSummaryResponse> getProductSummary(ProductSummaryRequest request) {
        log.info("Searching products");

        ProductSummaryQuery query = new ProductSummaryQuery(
                request.productName(),
                request.sku(),
                request.variantStatus(),
                request.categoryId(),
                request.productStatus(),
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

    public EntityModel<ProductAuditResponse> getProductAudit(String productId) {
        GetProductAuditResult result = queryBus.dispatch(new GetProductAuditQuery(productId));
        return EntityModel.of(productAuditDtoMapper.toResponse(result));
    }
}
