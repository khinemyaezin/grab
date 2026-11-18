package com.grab.service.sellerbff.internal.adapter.outbound.message;

import lombok.SneakyThrows;
import com.grab.service.contract.message.merchandising.catalog.CatalogProductPort;
import com.grab.service.contract.message.merchandising.catalog.CatalogVariantDefinitionPort;
import com.grab.service.sellerbff.internal.adapter.outbound.message.mapper.CreateCatalogProductMessageMapper;
import com.grab.service.sellerbff.internal.adapter.outbound.message.mapper.GetCatalogProductMessageMapper;
import com.grab.service.sellerbff.internal.adapter.outbound.message.mapper.GetCatalogVariantMessageMapper;
import com.grab.service.sellerbff.internal.adapter.outbound.message.mapper.ListCatalogVariantOptionsMessageMapper;
import com.grab.service.sellerbff.internal.adapter.outbound.message.mapper.ListCatalogVariantTypesMessageMapper;
import com.grab.service.sellerbff.internal.adapter.outbound.message.mapper.SearchCatalogProductsMessageMapper;
import com.grab.service.sellerbff.internal.adapter.outbound.message.mapper.UpdateCatalogProductMessageMapper;
import com.grab.service.sellerbff.internal.adapter.outbound.message.mapper.UpdateCatalogProductStatusMessageMapper;
import com.grab.service.sellerbff.port.outbound.SellerProductCatalogOutboundPort;
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
    @SneakyThrows
    public ProductOutboundResult createProduct(CreateProductRecord record) {
        CatalogProductPort.CreateProductMessage message = createMapper.toMessage(record);
        CatalogProductPort.ProductResult result = catalogProductPort.createProduct(message);
        return createMapper.toResult(result);
    }

    @Override
    @SneakyThrows
    public ProductOutboundResult updateProduct(Id merchantId, Id productId, UpdateProductRecord record) {
        CatalogProductPort.UpdateProductMessage message = updateMapper.toMessage(record);
        CatalogProductPort.ProductResult result = catalogProductPort.updateProduct(merchantId, productId, message);
        return updateMapper.toResult(result);
    }

    @Override
    @SneakyThrows
    public void deleteProduct(Id merchantId, Id productId) {
        catalogProductPort.deleteProduct(merchantId, productId);
    }

    @Override
    @SneakyThrows
    public ProductOutboundResult updateProductStatus(Id merchantId, Id productId, String status) {
        CatalogProductPort.ProductResult result = catalogProductPort.updateProductStatus(merchantId, productId, status);
        return updateStatusMapper.toResult(result);
    }

    @Override
    @SneakyThrows
    public Optional<ProductOutboundResult> findProductById(Id merchantId, Id productId) {
        return java.util.Optional.ofNullable(catalogProductPort.findProductById(merchantId, productId)).map(getProductMapper::toResult);
    }

    @Override
    @SneakyThrows
    public Optional<ProductOutboundResult> findProductBySlug(String slug) {
        return java.util.Optional.ofNullable(catalogProductPort.findProductBySlug(slug)).map(getProductMapper::toResult);
    }

    @Override
    @SneakyThrows
    public List<ProductSummaryOutboundResult> searchProducts(ProductSearchCriteriaRecord criteria) {
        CatalogProductPort.ProductSearchCriteria messageCriteria = searchMapper.toCriteria(criteria);
        List<CatalogProductPort.ProductSummaryResult> results = catalogProductPort.searchProducts(messageCriteria);
        return searchMapper.toResultList(results);
    }

    @Override
    @SneakyThrows
    public Optional<VariantOutboundResult> findVariantById(Id merchantId, Id productId, Id variantId) {
        return java.util.Optional.ofNullable(catalogProductPort.findVariantById(merchantId, productId, variantId)).map(getVariantMapper::toResult);
    }

    @Override
    @SneakyThrows
    public List<VariantTypeOutboundResult> listVariantTypes(String name) {
        List<CatalogVariantDefinitionPort.VariantTypeMessageResult> results = variantDefinitionPort.listVariantTypes(name);
        return listTypesMapper.toResultList(results);
    }

    @Override
    @SneakyThrows
    public List<VariantOptionOutboundResult> listVariantOptions(String name, String typeIdStr) {
        Id typeId = (typeIdStr != null && !typeIdStr.isBlank()) ? idGenerator.convertIdFrom(typeIdStr) : null;
        List<CatalogVariantDefinitionPort.VariantOptionMessageResult> results = variantDefinitionPort.listVariantOptions(name, typeId);
        return listOptionsMapper.toResultList(results);
    }
}
