package com.grab.app.platform.seller.internal.adapter.outbound.message;

import com.grab.app.merchandising.catalog.port.message.CatalogProductPort;
import com.grab.app.merchandising.catalog.port.message.CatalogVariantDefinitionPort;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.CreateCatalogProductMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.GetCatalogProductMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.GetCatalogVariantMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.ListCatalogVariantOptionsMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.ListCatalogVariantTypesMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.SearchCatalogProductsMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.UpdateCatalogProductMessageMapper;
import com.grab.app.platform.seller.internal.adapter.outbound.message.mapper.UpdateCatalogProductStatusMessageMapper;
import com.grab.app.platform.seller.port.outbound.SellerProductCatalogOutboundPort;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class CatalogProductMessageAdapter implements SellerProductCatalogOutboundPort {

    private final CatalogProductPort catalogProductPort;
    private final CatalogVariantDefinitionPort variantDefinitionPort;
    private final IdGenerator idGenerator;
    private final CreateCatalogProductMessageMapper createMapper;
    private final UpdateCatalogProductMessageMapper updateMapper;
    private final UpdateCatalogProductStatusMessageMapper updateStatusMapper;
    private final GetCatalogProductMessageMapper getProductMapper;
    private final SearchCatalogProductsMessageMapper searchMapper;
    private final GetCatalogVariantMessageMapper getVariantMapper;
    private final ListCatalogVariantTypesMessageMapper listTypesMapper;
    private final ListCatalogVariantOptionsMessageMapper listOptionsMapper;

    public CatalogProductMessageAdapter(
            CatalogProductPort catalogProductPort,
            CatalogVariantDefinitionPort variantDefinitionPort,
            IdGenerator idGenerator,
            CreateCatalogProductMessageMapper createMapper,
            UpdateCatalogProductMessageMapper updateMapper,
            UpdateCatalogProductStatusMessageMapper updateStatusMapper,
            GetCatalogProductMessageMapper getProductMapper,
            SearchCatalogProductsMessageMapper searchMapper,
            GetCatalogVariantMessageMapper getVariantMapper,
            ListCatalogVariantTypesMessageMapper listTypesMapper,
            ListCatalogVariantOptionsMessageMapper listOptionsMapper
    ) {
        this.catalogProductPort = catalogProductPort;
        this.variantDefinitionPort = variantDefinitionPort;
        this.idGenerator = idGenerator;
        this.createMapper = createMapper;
        this.updateMapper = updateMapper;
        this.updateStatusMapper = updateStatusMapper;
        this.getProductMapper = getProductMapper;
        this.searchMapper = searchMapper;
        this.getVariantMapper = getVariantMapper;
        this.listTypesMapper = listTypesMapper;
        this.listOptionsMapper = listOptionsMapper;
    }

    @Override
    public ProductOutboundResult createProduct(CreateProductRecord record) {
        CatalogProductPort.CreateProductMessage message = createMapper.toMessage(record);
        CatalogProductPort.ProductResult result = catalogProductPort.createProduct(message);
        return createMapper.toResult(result);
    }

    @Override
    public ProductOutboundResult updateProduct(Id merchantId, Id productId, UpdateProductRecord record) {
        CatalogProductPort.UpdateProductMessage message = updateMapper.toMessage(record);
        CatalogProductPort.ProductResult result = catalogProductPort.updateProduct(merchantId, productId, message);
        return updateMapper.toResult(result);
    }

    @Override
    public void deleteProduct(Id merchantId, Id productId) {
        catalogProductPort.deleteProduct(merchantId, productId);
    }

    @Override
    public ProductOutboundResult updateProductStatus(Id merchantId, Id productId, String status) {
        CatalogProductPort.ProductResult result = catalogProductPort.updateProductStatus(merchantId, productId, status);
        return updateStatusMapper.toResult(result);
    }

    @Override
    public Optional<ProductOutboundResult> findProductById(Id merchantId, Id productId) {
        return catalogProductPort.findProductById(merchantId, productId).map(getProductMapper::toResult);
    }

    @Override
    public Optional<ProductOutboundResult> findProductBySlug(String slug) {
        return catalogProductPort.findProductBySlug(slug).map(getProductMapper::toResult);
    }

    @Override
    public List<ProductSummaryOutboundResult> searchProducts(ProductSearchCriteriaRecord criteria) {
        CatalogProductPort.ProductSearchCriteria messageCriteria = searchMapper.toCriteria(criteria);
        List<CatalogProductPort.ProductSummaryResult> results = catalogProductPort.searchProducts(messageCriteria);
        return searchMapper.toResultList(results);
    }

    @Override
    public Optional<VariantOutboundResult> findVariantById(Id merchantId, Id productId, Id variantId) {
        return catalogProductPort.findVariantById(merchantId, productId, variantId).map(getVariantMapper::toResult);
    }

    @Override
    public List<VariantTypeOutboundResult> listVariantTypes(String name) {
        List<CatalogVariantDefinitionPort.VariantTypeMessageResult> results = variantDefinitionPort.listVariantTypes(name);
        return listTypesMapper.toResultList(results);
    }

    @Override
    public List<VariantOptionOutboundResult> listVariantOptions(String name, String typeIdStr) {
        Id typeId = (typeIdStr != null && !typeIdStr.isBlank()) ? idGenerator.convertIdFrom(typeIdStr) : null;
        List<CatalogVariantDefinitionPort.VariantOptionMessageResult> results = variantDefinitionPort.listVariantOptions(name, typeId);
        return listOptionsMapper.toResultList(results);
    }
}
