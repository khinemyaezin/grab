package com.grab.store.catalog.internal.api.adapter;

import com.catalog.application.model.read.CheckProductPublishedToChannelQuery;
import com.catalog.application.model.read.GetCatalogProductFactsQuery;
import com.catalog.application.model.read.GetCatalogProductFactsResult;
import com.catalog.application.model.read.GetCatalogVariantFactsQuery;
import com.catalog.application.model.read.GetProductResult;
import com.catalog.application.model.read.ListPublishedProductChannelsAfterQuery;
import com.catalog.application.model.read.ListPublishedProductChannelsAfterResult;
import com.catalog.application.model.read.PublishedProductChannel;
import com.catalog.application.port.inbound.CheckProductPublishedToChannelUseCase;
import com.catalog.application.port.inbound.GetCatalogProductFactsUseCase;
import com.catalog.application.port.inbound.GetCatalogVariantFactsUseCase;
import com.catalog.application.port.inbound.ListPublishedProductChannelsAfterUseCase;
import com.grab.framework.id.Id;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.catalog.internal.api.adapter.mapper.CatalogDiscoveryQueryMapper;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import com.grab.store.catalog.port.CatalogDiscoveryQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogDiscoveryQueryAdapterTest {

    private static final Id PRODUCT_ID = new CommonId("prod-1");
    private static final Id VARIANT_ID = new CommonId("var-1");
    private static final Id CHANNEL_ID = new CommonId("chan-1");
    private static final Id MERCHANT_ID = new CommonId("merch-1");
    private static final Id CATEGORY_ID = new CommonId("cat-1");

    @Mock
    private GetCatalogProductFactsUseCase getCatalogProductFactsUseCase;
    @Mock
    private GetCatalogVariantFactsUseCase getCatalogVariantFactsUseCase;
    @Mock
    private ListPublishedProductChannelsAfterUseCase listPublishedProductChannelsAfterUseCase;
    @Mock
    private CheckProductPublishedToChannelUseCase checkProductPublishedToChannelUseCase;
    @Mock
    private CatalogDiscoveryQueryMapper mapper;

    private CatalogDiscoveryQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CatalogDiscoveryQueryAdapter(
                getCatalogProductFactsUseCase,
                getCatalogVariantFactsUseCase,
                listPublishedProductChannelsAfterUseCase,
                checkProductPublishedToChannelUseCase,
                mapper
        );
    }

    @Test
    void findProduct_delegatesToUseCaseAndMapper() {
        GetProductResult productResult = mock(GetProductResult.class);
        GetCatalogProductFactsResult factsResult = new GetCatalogProductFactsResult("prod-1", "merch-1", productResult);
        CatalogDiscoveryQuery.ProductFacts expected = new CatalogDiscoveryQuery.ProductFacts(
                PRODUCT_ID, MERCHANT_ID, CATEGORY_ID, "Title", "slug", "Desc", "thumb.jpg", "ACTIVE", List.of(), List.of()
        );

        when(getCatalogProductFactsUseCase.execute(new GetCatalogProductFactsQuery("prod-1")))
                .thenReturn(Optional.of(factsResult));
        when(mapper.toFacts("prod-1", "merch-1", productResult)).thenReturn(expected);

        Optional<CatalogDiscoveryQuery.ProductFacts> actual = adapter.findProduct(PRODUCT_ID);

        assertThat(actual).contains(expected);
        verify(getCatalogProductFactsUseCase).execute(new GetCatalogProductFactsQuery("prod-1"));
        verify(mapper).toFacts("prod-1", "merch-1", productResult);
    }

    @Test
    void findVariant_delegatesToUseCaseAndMapper() {
        GetProductResult productResult = mock(GetProductResult.class);
        GetCatalogProductFactsResult factsResult = new GetCatalogProductFactsResult("prod-1", "merch-1", productResult);
        CatalogDiscoveryQuery.VariantFacts variantFacts = new CatalogDiscoveryQuery.VariantFacts(
                PRODUCT_ID, VARIANT_ID, "SKU-1", "Title", "ACTIVE", true, Map.of(), List.of(CHANNEL_ID)
        );
        CatalogDiscoveryQuery.ProductFacts productFacts = new CatalogDiscoveryQuery.ProductFacts(
                PRODUCT_ID, MERCHANT_ID, CATEGORY_ID, "Title", "slug", "Desc", "thumb.jpg", "ACTIVE", List.of(), List.of(variantFacts)
        );

        when(getCatalogVariantFactsUseCase.execute(new GetCatalogVariantFactsQuery("var-1")))
                .thenReturn(Optional.of(factsResult));
        when(mapper.toFacts("prod-1", "merch-1", productResult)).thenReturn(productFacts);

        Optional<CatalogDiscoveryQuery.VariantFacts> actual = adapter.findVariant(VARIANT_ID);

        assertThat(actual).contains(variantFacts);
        verify(getCatalogVariantFactsUseCase).execute(new GetCatalogVariantFactsQuery("var-1"));
    }

    @Test
    void findPublishedProductChannelsAfter_delegatesToUseCaseAndMapper() {
        PublishedProductChannel item = new PublishedProductChannel("prod-1", "chan-1");
        ListPublishedProductChannelsAfterResult result = new ListPublishedProductChannelsAfterResult(List.of(item));
        CatalogDiscoveryQuery.ProductChannelPair pair = new CatalogDiscoveryQuery.ProductChannelPair(PRODUCT_ID, CHANNEL_ID);

        when(listPublishedProductChannelsAfterUseCase.execute(any())).thenReturn(result);
        when(mapper.toProductChannelPairsFromCatalog(List.of(item))).thenReturn(List.of(pair));

        List<CatalogDiscoveryQuery.ProductChannelPair> actual = adapter.findPublishedProductChannelsAfter(
                PRODUCT_ID, CHANNEL_ID, 10
        );

        assertThat(actual).containsExactly(pair);
        verify(listPublishedProductChannelsAfterUseCase).execute(any(ListPublishedProductChannelsAfterQuery.class));
        verify(mapper).toProductChannelPairsFromCatalog(List.of(item));
    }

    @Test
    void isPublishedInChannel_delegatesToUseCase() {
        when(checkProductPublishedToChannelUseCase.execute(new CheckProductPublishedToChannelQuery("prod-1", "chan-1")))
                .thenReturn(true);

        boolean actual = adapter.isPublishedInChannel(PRODUCT_ID, CHANNEL_ID);

        assertThat(actual).isTrue();
        verify(checkProductPublishedToChannelUseCase).execute(new CheckProductPublishedToChannelQuery("prod-1", "chan-1"));
    }

    @Test
    void methodsAreAnnotatedWithCatalogReadTransactional() throws NoSuchMethodException {
        Method findProduct = CatalogDiscoveryQueryAdapter.class.getMethod("findProduct", Id.class);
        Method findVariant = CatalogDiscoveryQueryAdapter.class.getMethod("findVariant", Id.class);
        Method findChannels = CatalogDiscoveryQueryAdapter.class.getMethod("findPublishedProductChannelsAfter", Id.class, Id.class, int.class);
        Method isPublished = CatalogDiscoveryQueryAdapter.class.getMethod("isPublishedInChannel", Id.class, Id.class);

        assertThat(findProduct.isAnnotationPresent(CatalogReadTransactional.class)).isTrue();
        assertThat(findVariant.isAnnotationPresent(CatalogReadTransactional.class)).isTrue();
        assertThat(findChannels.isAnnotationPresent(CatalogReadTransactional.class)).isTrue();
        assertThat(isPublished.isAnnotationPresent(CatalogReadTransactional.class)).isTrue();
    }
}
