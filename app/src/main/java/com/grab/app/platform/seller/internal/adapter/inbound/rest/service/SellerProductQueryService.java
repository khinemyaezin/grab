package com.grab.app.platform.seller.internal.adapter.inbound.rest.service;

import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ProductResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.ProductSummaryResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.dto.response.VariantResponse;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetSellerProductBySlugMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetSellerProductMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.GetSellerVariantMapper;
import com.grab.app.platform.seller.internal.adapter.inbound.rest.mapper.SearchSellerProductsMapper;
import com.grab.app.platform.seller.internal.query.model.SellerProductQueries.GetSellerProductBySlugQuery;
import com.grab.app.platform.seller.internal.query.model.SellerProductQueries.GetSellerProductQuery;
import com.grab.app.platform.seller.internal.query.model.SellerProductQueries.GetSellerVariantQuery;
import com.grab.app.platform.seller.internal.query.model.SellerProductQueries.SearchSellerProductsQuery;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.ProductOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.ProductSummaryOutboundResult;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort.VariantOutboundResult;
import com.grab.framework.cqrs.query.QueryBus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SellerProductQueryService {

    private final QueryBus queryBus;
    private final GetSellerProductMapper getProductMapper;
    private final GetSellerProductBySlugMapper getProductBySlugMapper;
    private final SearchSellerProductsMapper searchProductsMapper;
    private final GetSellerVariantMapper getVariantMapper;

    public SellerProductQueryService(
            @Qualifier("sellerQueryBus") QueryBus queryBus,
            GetSellerProductMapper getProductMapper,
            GetSellerProductBySlugMapper getProductBySlugMapper,
            SearchSellerProductsMapper searchProductsMapper,
            GetSellerVariantMapper getVariantMapper) {
        this.queryBus = queryBus;
        this.getProductMapper = getProductMapper;
        this.getProductBySlugMapper = getProductBySlugMapper;
        this.searchProductsMapper = searchProductsMapper;
        this.getVariantMapper = getVariantMapper;
    }

    public Optional<ProductResponse> getProduct(String merchantIdStr, String productIdStr) {
        GetSellerProductQuery query = getProductMapper.toQuery(merchantIdStr, productIdStr);
        Optional<ProductOutboundResult> result = queryBus.dispatch(query);
        return result.map(getProductMapper::toResponse);
    }

    public Optional<ProductResponse> getProductBySlug(String slug) {
        GetSellerProductBySlugQuery query = getProductBySlugMapper.toQuery(slug);
        Optional<ProductOutboundResult> result = queryBus.dispatch(query);
        return result.map(getProductBySlugMapper::toResponse);
    }

    public Page<ProductSummaryResponse> searchProducts(String merchantIdStr, String query, int page, int size) {
        SearchSellerProductsQuery searchQuery = searchProductsMapper.toQuery(merchantIdStr, query, page, size);
        List<ProductSummaryOutboundResult> results = queryBus.dispatch(searchQuery);
        List<ProductSummaryResponse> content = results != null
                ? searchProductsMapper.toResponseList(results)
                : List.of();
        return new PageImpl<>(content, PageRequest.of(page, Math.max(size, 1)), content.size());
    }

    public Optional<VariantResponse> getVariant(String merchantIdStr, String productIdStr, String variantIdStr) {
        GetSellerVariantQuery query = getVariantMapper.toQuery(merchantIdStr, productIdStr, variantIdStr);
        Optional<VariantOutboundResult> result = queryBus.dispatch(query);
        return result.map(getVariantMapper::toResponse);
    }
}
