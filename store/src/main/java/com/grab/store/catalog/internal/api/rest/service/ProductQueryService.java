package com.grab.store.catalog.internal.api.rest.service;

import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.api.rest.dto.request.VariationMatrixRequest;
import com.grab.store.catalog.internal.api.rest.dto.request.ProductSummaryRequest;
import com.grab.store.catalog.internal.api.rest.dto.response.*;
import com.grab.store.catalog.internal.api.rest.mapper.*;
import com.grab.store.catalog.internal.query.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private static final Logger log = Loggers.getLogger(ProductQueryService.class);

    private final QueryBus queryBus;
    private final GetProductDtoMapper getProductDtoMapper;
    private final GetProductBySlugDtoMapper getProductBySlugDtoMapper;
    private final VariationMatrixQueryMapper variationMatrixQueryMapper;
    private final ProductSummaryDtoMapper productSummaryDtoMapper;
    private final ProductAuditDtoMapper productAuditDtoMapper;

    public GetProductResponse getProduct(String productId) {
        log.info("Getting product: {}", productId);

        GetProductQuery query = new GetProductQuery(productId);
        GetProductResult result = queryBus.dispatch(query);
        return getProductDtoMapper.toResponse(result);
    }

    public VariationMatrixResponse getMatrixCombination(VariationMatrixRequest request) {
        log.info("Product combination");

        VariationMatrixQuery query = variationMatrixQueryMapper.toQuery(request);
        VariationMatrixResult result = queryBus.dispatch(query);
        return variationMatrixQueryMapper.toResponse(result);
    }

    public Page<ProductSummaryResponse> getProductSummary(ProductSummaryRequest request, Pageable pageable) {
        log.info("Searching products");
        ProductSummaryQuery query = productSummaryDtoMapper.toQuery(request, pageable);
        Page<ProductSummaryResult> result = queryBus.dispatch(query);
        return result.map(productSummaryDtoMapper::toResponse);
    }

    public GetProductBySlugResponse getProductBySlug(String slug) {
        log.info("Getting product by slug: {}", slug);

        GetProductBySlugQuery query = new GetProductBySlugQuery(slug);
        GetProductBySlugResult result = queryBus.dispatch(query);
        
        return getProductBySlugDtoMapper.toResponse(result);
    }

    public ProductAuditResponse getProductAudit(String productId) {
        GetProductAuditResult result = queryBus.dispatch(new GetProductAuditQuery(productId));
        return productAuditDtoMapper.toResponse(result);
    }
}
