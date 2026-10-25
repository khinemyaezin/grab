package com.grab.app.productmerchandising.catalog.internal.api.rest.service;

import com.grab.app.productmerchandising.catalog.api.representation.GetProductResponse;
import com.grab.app.productmerchandising.catalog.api.representation.GetVariantResponse;
import com.grab.app.productmerchandising.catalog.api.representation.ProductPublicationResponse;
import com.grab.app.productmerchandising.catalog.api.representation.ProductSearchResponse;

import com.catalog.application.model.read.*;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.app.productmerchandising.catalog.internal.api.rest.dto.request.ProductSearchRequest;
import com.grab.app.productmerchandising.catalog.internal.api.rest.dto.request.ProductVariantSearchRequest;
import com.grab.app.productmerchandising.catalog.internal.api.rest.dto.request.VariationMatrixRequest;
import com.grab.app.productmerchandising.catalog.internal.api.rest.dto.response.*;
import com.grab.app.productmerchandising.catalog.internal.api.rest.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductQueryService {

    private static final Logger log = Loggers.getLogger(ProductQueryService.class);

    private final QueryBus queryBus;
    private final GetProductDtoMapper getProductDtoMapper;
    private final GetProductBySlugDtoMapper getProductBySlugDtoMapper;
    private final VariationMatrixQueryMapper variationMatrixQueryMapper;
    private final ProductSummaryDtoMapper productSummaryDtoMapper;
    private final ProductVariantSummaryDtoMapper productVariantSummaryDtoMapper;
    private final ProductAuditDtoMapper productAuditDtoMapper;
    private final GetVariantDtoMapper getVariantDtoMapper;
    private final AuthenticatedCatalogMerchantResolver merchantResolver;

    public GetProductResponse getProduct(String productId) {
        log.info("Getting product: {}", productId);

        String merchantId = merchantResolver.resolveCurrentMerchantId();
        GetProductQuery query = new GetProductQuery(merchantId, productId);
        GetProductResult result = queryBus.dispatch(query);
        return getProductDtoMapper.toResponse(result);
    }

    public GetVariantResponse getVariant(String productId, String variantId) {
        log.info("Getting variant: {} for product: {}", variantId, productId);

        String merchantId = merchantResolver.resolveCurrentMerchantId();
        GetVariantQuery query = getVariantDtoMapper.toQuery(merchantId, productId, variantId);
        GetVariantResult result = queryBus.dispatch(query);
        return getVariantDtoMapper.toResponse(result);
    }

    public VariationMatrixResponse getMatrixCombination(VariationMatrixRequest request) {
        log.info("Product combination");

        VariationMatrixQuery query = variationMatrixQueryMapper.toQuery(request);
        VariationMatrixResult result = queryBus.dispatch(query);
        return variationMatrixQueryMapper.toResponse(result);
    }

    public Page<ProductSearchResponse> getProductSummary(ProductSearchRequest request, Pageable pageable) {
        log.info("Searching products");
        String merchantId = merchantResolver.resolveCurrentMerchantId();
        ProductSearchQuery query = productSummaryDtoMapper.toQuery(merchantId, request, pageable);
        Page<ProductSearchResult> result = queryBus.dispatch(query);
        return result.map(productSummaryDtoMapper::toResponse);
    }

    public Page<ProductVariantSearchResponse> getProductVariantSummary(
            ProductVariantSearchRequest request,
            Pageable pageable
    ) {
        log.info("Searching product variants");
        String merchantId = merchantResolver.resolveCurrentMerchantId();
        ProductVariantSummaryQuery query = productVariantSummaryDtoMapper.toQuery(merchantId, request, pageable);
        Page<ProductVariantSummaryResult> result = queryBus.dispatch(query);
        return result.map(productVariantSummaryDtoMapper::toResponse);
    }

    public GetProductBySlugResponse getProductBySlug(String slug) {
        log.info("Getting product by slug: {}", slug);

        GetProductBySlugQuery query = new GetProductBySlugQuery(slug);
        GetProductBySlugResult result = queryBus.dispatch(query);
        return getProductBySlugDtoMapper.toResponse(result);
    }

    public ProductAuditResponse getProductAudit(String productId) {
        String merchantId = merchantResolver.resolveCurrentMerchantId();
        GetProductAuditQuery query = new GetProductAuditQuery(merchantId, productId);
        GetProductAuditResult result = queryBus.dispatch(query);
        return productAuditDtoMapper.toResponse(result);
    }

    public ProductPublicationResponse listProductPublications(String productId) {
        log.info("Listing product publications: {}", productId);
        String merchantId = merchantResolver.resolveCurrentMerchantId();
        List<ProductPublicationItem> publications = queryBus.dispatch(
                new ListProductPublicationsQuery(merchantId, productId)
        );
        return new ProductPublicationResponse(
                productId,
                publications.stream()
                        .map(item -> new ProductPublicationResponse.Publication(
                                item.variantId(),
                                item.salesChannelId()
                        ))
                        .toList()
        );
    }
}
