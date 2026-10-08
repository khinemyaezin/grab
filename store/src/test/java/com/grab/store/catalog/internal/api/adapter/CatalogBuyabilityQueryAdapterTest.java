package com.grab.store.catalog.internal.api.adapter;

import com.catalog.application.model.read.*;
import com.catalog.application.port.inbound.*;
import com.grab.store.catalog.internal.api.adapter.mapper.CatalogBuyabilityQueryMapper;
import com.grab.store.catalog.port.CatalogBuyabilityQuery.CatalogVariantSlice;
import com.grab.store.catalog.port.CatalogBuyabilityQuery.PublicationSlice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogBuyabilityQueryAdapterTest {

    @Mock
    private FindPublishedVariantUseCase findPublishedVariantUseCase;
    @Mock
    private FindCatalogVariantSliceUseCase findCatalogVariantSliceUseCase;
    @Mock
    private ListCatalogPublicationsUseCase listCatalogPublicationsUseCase;
    @Mock
    private ListVariantIdsForProductUseCase listVariantIdsForProductUseCase;
    @Mock
    private ListSalesChannelIdsForVariantUseCase listSalesChannelIdsForVariantUseCase;

    private CatalogBuyabilityQueryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new CatalogBuyabilityQueryAdapter(
                findPublishedVariantUseCase,
                findCatalogVariantSliceUseCase,
                listCatalogPublicationsUseCase,
                listVariantIdsForProductUseCase,
                listSalesChannelIdsForVariantUseCase,
                new CatalogBuyabilityQueryMapper()
        );
    }

    @Test
    void findPublished_validVariantAndSalesChannel_returnsCatalogVariantSlice() {
        FindPublishedVariantResult result = new FindPublishedVariantResult(
                "v1", "p1", "s1", "SKU1", "Title", "slug", "ACTIVE", false, "img.jpg"
        );
        when(findPublishedVariantUseCase.execute(any(FindPublishedVariantQuery.class)))
                .thenReturn(Optional.of(result));

        Optional<CatalogVariantSlice> slice = adapter.findPublished("v1", "sc1");

        assertThat(slice).isPresent();
        assertThat(slice.get().variantId()).isEqualTo("v1");
        assertThat(slice.get().active()).isTrue();
    }

    @Test
    void findVariant_validVariantId_returnsCatalogVariantSlice() {
        FindCatalogVariantSliceResult result = new FindCatalogVariantSliceResult(
                "v1", "p1", "s1", "SKU1", "Title", "slug", "ACTIVE", false, "img.jpg"
        );
        when(findCatalogVariantSliceUseCase.execute(any(FindCatalogVariantSliceQuery.class)))
                .thenReturn(Optional.of(result));

        Optional<CatalogVariantSlice> slice = adapter.findVariant("v1");

        assertThat(slice).isPresent();
        assertThat(slice.get().variantId()).isEqualTo("v1");
    }

    @Test
    void variantIdsForProduct_validProductId_returnsVariantIdList() {
        when(listVariantIdsForProductUseCase.execute(any(ListVariantIdsForProductQuery.class)))
                .thenReturn(List.of("v1", "v2"));

        List<String> variantIds = adapter.variantIdsForProduct("p1");

        assertThat(variantIds).containsExactly("v1", "v2");
    }

    @Test
    void listPublications_existingPublications_returnsPublicationSliceList() {
        when(listCatalogPublicationsUseCase.execute(any(ListCatalogPublicationsQuery.class)))
                .thenReturn(List.of(new CatalogPublicationItem("v1", "sc1")));

        List<PublicationSlice> publications = adapter.listPublications();

        assertThat(publications).hasSize(1);
        assertThat(publications.getFirst().variantId()).isEqualTo("v1");
        assertThat(publications.getFirst().salesChannelId()).isEqualTo("sc1");
    }

    @Test
    void salesChannelIdsForVariant_validVariantId_returnsSalesChannelIdList() {
        when(listSalesChannelIdsForVariantUseCase.execute(any(ListSalesChannelIdsForVariantQuery.class)))
                .thenReturn(List.of("sc1", "sc2"));

        List<String> scIds = adapter.salesChannelIdsForVariant("v1");

        assertThat(scIds).containsExactly("sc1", "sc2");
    }
}
