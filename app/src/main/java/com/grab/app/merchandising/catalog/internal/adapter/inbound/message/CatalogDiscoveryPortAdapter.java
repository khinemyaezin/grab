package com.grab.app.merchandising.catalog.internal.adapter.inbound.message;

import com.catalog.application.model.read.CatalogPublicationItem;
import com.catalog.application.model.read.CheckProductPublishedToChannelQuery;
import com.catalog.application.model.read.FindCatalogVariantSliceQuery;
import com.catalog.application.model.read.FindCatalogVariantSliceResult;
import com.catalog.application.model.read.FindPublishedVariantQuery;
import com.catalog.application.model.read.FindPublishedVariantResult;
import com.catalog.application.model.read.GetCatalogProductFactsQuery;
import com.catalog.application.model.read.GetCatalogProductFactsResult;
import com.catalog.application.model.read.GetCatalogVariantFactsQuery;
import com.catalog.application.model.read.ListCatalogPublicationsQuery;
import com.catalog.application.model.read.ListPublishedProductChannelsAfterQuery;
import com.catalog.application.model.read.ListPublishedProductChannelsAfterResult;
import com.catalog.application.model.read.ListPublishedProductChannelsBySkuQuery;
import com.catalog.application.model.read.ListPublishedProductChannelsForSalesChannelAfterQuery;
import com.catalog.application.model.read.ListVariantIdsForProductQuery;
import com.catalog.application.model.read.PublishedProductChannel;
import com.catalog.application.port.inbound.CheckProductPublishedToChannelUseCase;
import com.catalog.application.port.inbound.FindCatalogVariantSliceUseCase;
import com.catalog.application.port.inbound.FindPublishedVariantUseCase;
import com.catalog.application.port.inbound.ListCatalogPublicationsUseCase;
import com.catalog.application.port.inbound.ListPublishedProductChannelsAfterUseCase;
import com.catalog.application.port.inbound.ListPublishedProductChannelsBySkuUseCase;
import com.catalog.application.port.inbound.ListPublishedProductChannelsForSalesChannelAfterUseCase;
import com.catalog.application.port.inbound.ListVariantIdsForProductUseCase;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.FindCatalogVariantSlicePortMapper;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.FindPublishedVariantPortMapper;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.GetCatalogProductFactsPortMapper;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.GetCatalogVariantFactsPortMapper;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.ListCatalogPublicationsPortMapper;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.ListProductChannelsAfterPortMapper;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.ListProductChannelsBySkuPortMapper;
import com.grab.app.merchandising.catalog.internal.adapter.inbound.message.mapper.ListProductChannelsForSalesChannelPortMapper;
import com.grab.app.merchandising.catalog.port.message.CatalogDiscoveryPort;
import com.grab.framework.cqrs.query.QueryBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class CatalogDiscoveryPortAdapter implements CatalogDiscoveryPort {

    private final QueryBus queryBus;
    private final CheckProductPublishedToChannelUseCase checkProductPublishedToChannelUseCase;
    private final FindPublishedVariantUseCase findPublishedVariantUseCase;
    private final FindCatalogVariantSliceUseCase findCatalogVariantSliceUseCase;
    private final ListCatalogPublicationsUseCase listCatalogPublicationsUseCase;
    private final ListPublishedProductChannelsAfterUseCase listPublishedProductChannelsAfterUseCase;
    private final ListPublishedProductChannelsBySkuUseCase listPublishedProductChannelsBySkuUseCase;
    private final ListPublishedProductChannelsForSalesChannelAfterUseCase listPublishedProductChannelsForSalesChannelAfterUseCase;
    private final ListVariantIdsForProductUseCase listVariantIdsForProductUseCase;
    private final IdGenerator idGenerator;
    private final GetCatalogProductFactsPortMapper factsMapper;
    private final GetCatalogVariantFactsPortMapper variantFactsMapper;
    private final FindCatalogVariantSlicePortMapper variantSliceMapper;
    private final FindPublishedVariantPortMapper publishedVariantMapper;
    private final ListCatalogPublicationsPortMapper publicationsMapper;
    private final ListProductChannelsAfterPortMapper channelsAfterMapper;
    private final ListProductChannelsBySkuPortMapper channelsBySkuMapper;
    private final ListProductChannelsForSalesChannelPortMapper channelsForChannelMapper;

    public CatalogDiscoveryPortAdapter(
            @Qualifier("merchandisingQueryBus") QueryBus queryBus,
            CheckProductPublishedToChannelUseCase checkProductPublishedToChannelUseCase,
            FindPublishedVariantUseCase findPublishedVariantUseCase,
            FindCatalogVariantSliceUseCase findCatalogVariantSliceUseCase,
            ListCatalogPublicationsUseCase listCatalogPublicationsUseCase,
            ListPublishedProductChannelsAfterUseCase listPublishedProductChannelsAfterUseCase,
            ListPublishedProductChannelsBySkuUseCase listPublishedProductChannelsBySkuUseCase,
            ListPublishedProductChannelsForSalesChannelAfterUseCase listPublishedProductChannelsForSalesChannelAfterUseCase,
            ListVariantIdsForProductUseCase listVariantIdsForProductUseCase,
            IdGenerator idGenerator,
            GetCatalogProductFactsPortMapper factsMapper,
            GetCatalogVariantFactsPortMapper variantFactsMapper,
            FindCatalogVariantSlicePortMapper variantSliceMapper,
            FindPublishedVariantPortMapper publishedVariantMapper,
            ListCatalogPublicationsPortMapper publicationsMapper,
            ListProductChannelsAfterPortMapper channelsAfterMapper,
            ListProductChannelsBySkuPortMapper channelsBySkuMapper,
            ListProductChannelsForSalesChannelPortMapper channelsForChannelMapper) {
        this.queryBus = queryBus;
        this.checkProductPublishedToChannelUseCase = checkProductPublishedToChannelUseCase;
        this.findPublishedVariantUseCase = findPublishedVariantUseCase;
        this.findCatalogVariantSliceUseCase = findCatalogVariantSliceUseCase;
        this.listCatalogPublicationsUseCase = listCatalogPublicationsUseCase;
        this.listPublishedProductChannelsAfterUseCase = listPublishedProductChannelsAfterUseCase;
        this.listPublishedProductChannelsBySkuUseCase = listPublishedProductChannelsBySkuUseCase;
        this.listPublishedProductChannelsForSalesChannelAfterUseCase = listPublishedProductChannelsForSalesChannelAfterUseCase;
        this.listVariantIdsForProductUseCase = listVariantIdsForProductUseCase;
        this.idGenerator = idGenerator;
        this.factsMapper = factsMapper;
        this.variantFactsMapper = variantFactsMapper;
        this.variantSliceMapper = variantSliceMapper;
        this.publishedVariantMapper = publishedVariantMapper;
        this.publicationsMapper = publicationsMapper;
        this.channelsAfterMapper = channelsAfterMapper;
        this.channelsBySkuMapper = channelsBySkuMapper;
        this.channelsForChannelMapper = channelsForChannelMapper;
    }

    @Override
    public Optional<ProductFactsResult> findProductFacts(Id productId) {
        if (productId == null) {
            return Optional.empty();
        }
        GetCatalogProductFactsQuery query = factsMapper.toQuery(productId);
        GetCatalogProductFactsResult result = queryBus.dispatch(query);
        return Optional.ofNullable(factsMapper.toResponse(result));
    }

    @Override
    public Optional<VariantFactsResult> findVariantFacts(Id variantId) {
        if (variantId == null) {
            return Optional.empty();
        }
        GetCatalogVariantFactsQuery query = variantFactsMapper.toQuery(variantId);
        GetCatalogProductFactsResult result = queryBus.dispatch(query);
        return variantFactsMapper.toResponse(result, variantId);
    }

    @Override
    public boolean isPublishedInChannel(Id productId, Id salesChannelId) {
        if (productId == null || salesChannelId == null) {
            return false;
        }
        CheckProductPublishedToChannelQuery query = new CheckProductPublishedToChannelQuery(
                productId.getValue(), salesChannelId.getValue()
        );
        return checkProductPublishedToChannelUseCase.execute(query);
    }

    @Override
    public Optional<CatalogVariantSliceResult> findPublishedVariantSlice(Id variantId, Id salesChannelId) {
        if (variantId == null || salesChannelId == null) {
            return Optional.empty();
        }
        FindPublishedVariantQuery query = publishedVariantMapper.toQuery(variantId, salesChannelId);
        Optional<FindPublishedVariantResult> result = findPublishedVariantUseCase.execute(query);
        return result.map(publishedVariantMapper::toResponse);
    }

    @Override
    public Optional<CatalogVariantSliceResult> findVariant(Id variantId) {
        if (variantId == null) {
            return Optional.empty();
        }
        FindCatalogVariantSliceQuery query = variantSliceMapper.toQuery(variantId);
        Optional<FindCatalogVariantSliceResult> slice = findCatalogVariantSliceUseCase.execute(query);
        return slice.map(variantSliceMapper::toResponse);
    }

    @Override
    public List<Id> variantIdsForProduct(Id productId) {
        if (productId == null) {
            return Collections.emptyList();
        }
        ListVariantIdsForProductQuery query = new ListVariantIdsForProductQuery(productId.getValue());
        List<String> ids = listVariantIdsForProductUseCase.execute(query);
        if (ids == null) {
            return Collections.emptyList();
        }
        return ids.stream().map(idGenerator::convertIdFrom).toList();
    }

    @Override
    public List<PublicationSliceResult> listPublications() {
        ListCatalogPublicationsQuery query = publicationsMapper.toQuery();
        List<CatalogPublicationItem> items = listCatalogPublicationsUseCase.execute(query);
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream().map(publicationsMapper::toResponse).toList();
    }

    @Override
    public List<ProductChannelPairResult> findPublishedProductChannelsAfter(Id productId, Id salesChannelId, int limit) {
        ListPublishedProductChannelsAfterQuery query = channelsAfterMapper.toQuery(productId, salesChannelId, limit);
        ListPublishedProductChannelsAfterResult result = listPublishedProductChannelsAfterUseCase.execute(query);
        if (result == null || result.items() == null) {
            return Collections.emptyList();
        }
        return result.items().stream().map(channelsAfterMapper::toResponse).toList();
    }

    @Override
    public List<ProductChannelPairResult> findPublishedProductChannelsBySku(String sku) {
        if (sku == null || sku.isBlank()) {
            return Collections.emptyList();
        }
        ListPublishedProductChannelsBySkuQuery query = channelsBySkuMapper.toQuery(sku);
        List<PublishedProductChannel> items = listPublishedProductChannelsBySkuUseCase.execute(query);
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream().map(channelsBySkuMapper::toResponse).toList();
    }

    @Override
    public List<ProductChannelPairResult> findPublishedProductChannelsForSalesChannelAfter(Id salesChannelId, Id afterProductId, int limit) {
        if (salesChannelId == null) {
            return Collections.emptyList();
        }
        ListPublishedProductChannelsForSalesChannelAfterQuery query = channelsForChannelMapper.toQuery(salesChannelId, afterProductId, limit);
        List<PublishedProductChannel> items = listPublishedProductChannelsForSalesChannelAfterUseCase.execute(query);
        if (items == null) {
            return Collections.emptyList();
        }
        return items.stream().map(channelsForChannelMapper::toResponse).toList();
    }
}
