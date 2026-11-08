package com.grab.app.merchandising.catalog.internal.adapter.inbound.message;

import com.catalog.application.model.read.GetProductBySlugQuery;
import com.catalog.application.model.read.GetProductBySlugResult;
import com.catalog.application.model.read.GetProductQuery;
import com.catalog.application.model.read.GetProductResult;
import com.catalog.application.model.read.GetVariantQuery;
import com.catalog.application.model.read.GetVariantResult;
import com.catalog.application.model.read.ProductSearchQuery;
import com.catalog.application.model.read.ProductSearchResult;
import com.catalog.application.model.write.CreateProductSetCommand;
import com.catalog.application.model.write.CreateProductSetResult;
import com.catalog.application.model.write.DeleteProductCommand;
import com.catalog.application.model.write.UpdateProductCommand;
import com.catalog.application.model.write.UpdateProductResult;
import com.catalog.application.model.write.UpdateProductStatusCommand;
import com.catalog.application.model.write.UpdateProductStatusResult;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.CreateProductPortMapper;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.DeleteProductPortMapper;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.GetProductBySlugPortMapper;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.GetProductPortMapper;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.GetVariantPortMapper;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.SearchProductsPortMapper;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.UpdateProductPortMapper;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.UpdateProductStatusPortMapper;
import com.grab.app.merchandising.catalog.port.message.CatalogProductPort;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class CatalogProductPortAdapter implements CatalogProductPort {

    private final CommandBus commandBus;
    private final QueryBus queryBus;
    private final CreateProductPortMapper createProductMapper;
    private final UpdateProductPortMapper updateProductMapper;
    private final DeleteProductPortMapper deleteProductMapper;
    private final UpdateProductStatusPortMapper updateProductStatusMapper;
    private final GetProductPortMapper getProductMapper;
    private final GetProductBySlugPortMapper getProductBySlugMapper;
    private final SearchProductsPortMapper searchProductsMapper;
    private final GetVariantPortMapper getVariantMapper;

    public CatalogProductPortAdapter(
            @Qualifier("merchandisingCommandBus") CommandBus commandBus,
            @Qualifier("merchandisingQueryBus") QueryBus queryBus,
            CreateProductPortMapper createProductMapper,
            UpdateProductPortMapper updateProductMapper,
            DeleteProductPortMapper deleteProductMapper,
            UpdateProductStatusPortMapper updateProductStatusMapper,
            GetProductPortMapper getProductMapper,
            GetProductBySlugPortMapper getProductBySlugMapper,
            SearchProductsPortMapper searchProductsMapper,
            GetVariantPortMapper getVariantMapper) {
        this.commandBus = commandBus;
        this.queryBus = queryBus;
        this.createProductMapper = createProductMapper;
        this.updateProductMapper = updateProductMapper;
        this.deleteProductMapper = deleteProductMapper;
        this.updateProductStatusMapper = updateProductStatusMapper;
        this.getProductMapper = getProductMapper;
        this.getProductBySlugMapper = getProductBySlugMapper;
        this.searchProductsMapper = searchProductsMapper;
        this.getVariantMapper = getVariantMapper;
    }

    @Override
    public ProductResult createProduct(CreateProductMessage message) {
        CreateProductSetCommand command = createProductMapper.toCommand(message);
        CreateProductSetResult result = commandBus.dispatch(command);
        return createProductMapper.toResponse(result, message);
    }

    @Override
    public ProductResult updateProduct(Id merchantId, Id productId, UpdateProductMessage message) {
        UpdateProductCommand command = updateProductMapper.toCommand(merchantId, productId, message);
        UpdateProductResult result = commandBus.dispatch(command);
        return updateProductMapper.toResponse(result);
    }

    @Override
    public void deleteProduct(Id merchantId, Id productId) {
        DeleteProductCommand command = deleteProductMapper.toCommand(merchantId, productId);
        commandBus.dispatch(command);
    }

    @Override
    public ProductResult updateProductStatus(Id merchantId, Id productId, String status) {
        UpdateProductStatusCommand command = updateProductStatusMapper.toCommand(merchantId, productId, status);
        UpdateProductStatusResult result = commandBus.dispatch(command);
        return updateProductStatusMapper.toResponse(result);
    }

    @Override
    public Optional<ProductResult> findProductById(Id merchantId, Id productId) {
        GetProductQuery query = getProductMapper.toQuery(merchantId, productId);
        if (query == null) {
            return Optional.empty();
        }
        GetProductResult result = queryBus.dispatch(query);
        return Optional.ofNullable(getProductMapper.toResponse(result));
    }

    @Override
    public Optional<ProductResult> findProductBySlug(String slug) {
        GetProductBySlugQuery query = getProductBySlugMapper.toQuery(slug);
        if (query == null) {
            return Optional.empty();
        }
        GetProductBySlugResult result = queryBus.dispatch(query);
        return Optional.ofNullable(getProductBySlugMapper.toResponse(result));
    }

    @Override
    public List<ProductSummaryResult> searchProducts(ProductSearchCriteria criteria) {
        ProductSearchQuery query = searchProductsMapper.toQuery(criteria);
        Page<ProductSearchResult> results = queryBus.dispatch(query);
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }
        return results.map(searchProductsMapper::toResponse).toList();
    }

    @Override
    public Optional<VariantResult> findVariantById(Id merchantId, Id productId, Id variantId) {
        GetVariantQuery query = getVariantMapper.toQuery(merchantId, productId, variantId);
        if (query == null) {
            return Optional.empty();
        }
        GetVariantResult result = queryBus.dispatch(query);
        return Optional.ofNullable(getVariantMapper.toResponse(result));
    }
}
