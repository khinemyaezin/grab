package com.grab.store.cart.internal.adapter;

import com.cart.application.port.outbound.CatalogBuyabilityPort;
import com.grab.store.catalog.port.CatalogBuyabilityQuery;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogBuyabilityAdapterTest {

    @Mock
    private CatalogBuyabilityQuery catalogBuyabilityQuery;

    @InjectMocks
    private CatalogBuyabilityAdapter adapter;

    @Test
    void findPublished_notPublished_returnsEmpty() {
        when(catalogBuyabilityQuery.findPublished("var-1", "web-1")).thenReturn(Optional.empty());

        assertThat(adapter.findPublished("var-1", "web-1")).isEmpty();
    }

    @Test
    void findPublished_published_returnsMappedBuyableVariant() {
        when(catalogBuyabilityQuery.findPublished("var-1", "web-1"))
                .thenReturn(Optional.of(publishedVariant()));

        Optional<CatalogBuyabilityPort.BuyableVariant> result = adapter.findPublished("var-1", "web-1");

        assertThat(result).hasValueSatisfying(v -> {
            assertThat(v.variantId()).isEqualTo("var-1");
            assertThat(v.sellerId()).isEqualTo("seller-1");
            assertThat(v.sku()).isEqualTo("SKU-1");
            assertThat(v.title()).isEqualTo("Item");
            assertThat(v.untracked()).isFalse();
        });
    }

    private static CatalogBuyabilityQuery.CatalogVariantSlice publishedVariant() {
        return new CatalogBuyabilityQuery.CatalogVariantSlice(
                "var-1",
                "prod-1",
                "seller-1",
                "SKU-1",
                "Item",
                "item",
                "ACTIVE",
                false,
                null
        );
    }
}
