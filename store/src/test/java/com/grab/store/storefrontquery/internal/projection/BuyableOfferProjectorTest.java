package com.grab.store.storefrontquery.internal.projection;

import com.grab.store.catalog.query.CatalogBuyabilityQueryPort;
import com.grab.store.inventory.query.InventoryAvailabilityQueryPort;
import com.grab.store.pricing.query.PricingQuoteQueryPort;
import com.grab.store.saleschannel.query.SalesChannelQueryPort;
import com.storefrontquery.infrastructure.entity.BuyableOfferEntity;
import com.storefrontquery.infrastructure.repository.jpa.BuyableOfferJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuyableOfferProjectorTest {

    @Mock
    private BuyableOfferJpaRepository offers;
    @Mock
    private CatalogBuyabilityQueryPort catalog;
    @Mock
    private PricingQuoteQueryPort pricing;
    @Mock
    private InventoryAvailabilityQueryPort inventory;
    @Mock
    private SalesChannelQueryPort salesChannels;

    private BuyableOfferProjector projector;

    @BeforeEach
    void setUp() {
        projector = new BuyableOfferProjector(offers, catalog, pricing, inventory, salesChannels);
    }

    @Test
    void onPublished_shouldMarkBuyableWhenActivePricedAndInStock() {
        when(offers.findBySalesChannelIdAndVariantId("web-1", "var-1")).thenReturn(Optional.empty());
        when(catalog.findVariant("var-1")).thenReturn(Optional.of(new CatalogBuyabilityQueryPort.CatalogVariantSlice(
                "var-1", "prod-1", "seller-1", "SKU-1", "Shirt", "shirt", "ACTIVE", false, "img"
        )));
        when(salesChannels.isEnabled("web-1")).thenReturn(true);
        when(inventory.available("SKU-1", "web-1"))
                .thenReturn(new InventoryAvailabilityQueryPort.Availability(4, false));
        when(pricing.quote(eq("var-1"), eq("MMK"), eq(1), eq("web-1")))
                .thenReturn(Optional.of(new PricingQuoteQueryPort.QuotedPrice(new BigDecimal("12000"), "MMK", "ps-1")));
        when(offers.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        projector.onPublished("var-1", "web-1", Instant.now());

        ArgumentCaptor<BuyableOfferEntity> captor = ArgumentCaptor.forClass(BuyableOfferEntity.class);
        verify(offers).save(captor.capture());
        BuyableOfferEntity saved = captor.getValue();
        assertThat(saved.isPublished()).isTrue();
        assertThat(saved.isBuyable()).isTrue();
        assertThat(saved.getAmount()).isEqualByComparingTo("12000");
        assertThat(saved.getAvailableQty()).isEqualTo(4);
    }

    @Test
    void onPublished_shouldStayNotBuyableWhenStockIsAtUnroutedLocation() {
        when(offers.findBySalesChannelIdAndVariantId("mkt-1", "var-1")).thenReturn(Optional.empty());
        when(catalog.findVariant("var-1")).thenReturn(Optional.of(new CatalogBuyabilityQueryPort.CatalogVariantSlice(
                "var-1", "prod-1", "seller-1", "SKU-1", "Shirt", "shirt", "ACTIVE", false, "img"
        )));
        when(salesChannels.isEnabled("mkt-1")).thenReturn(true);
        when(inventory.available("SKU-1", "mkt-1"))
                .thenReturn(new InventoryAvailabilityQueryPort.Availability(0, false));
        when(pricing.quote(eq("var-1"), eq("MMK"), eq(1), eq("mkt-1")))
                .thenReturn(Optional.of(new PricingQuoteQueryPort.QuotedPrice(new BigDecimal("12000"), "MMK", "ps-1")));
        when(offers.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        projector.onPublished("var-1", "mkt-1", Instant.now());

        ArgumentCaptor<BuyableOfferEntity> captor = ArgumentCaptor.forClass(BuyableOfferEntity.class);
        verify(offers).save(captor.capture());
        assertThat(captor.getValue().isBuyable()).isFalse();
        assertThat(captor.getValue().getAvailableQty()).isZero();
    }

    @Test
    void onUnpublished_shouldIgnoreStaleEvent() {
        BuyableOfferEntity existing = new BuyableOfferEntity();
        existing.setSalesChannelId("web-1");
        existing.setVariantId("var-1");
        existing.setPublished(true);
        existing.setUpdatedAt(Instant.parse("2026-01-02T00:00:00Z"));
        when(offers.findBySalesChannelIdAndVariantId("web-1", "var-1")).thenReturn(Optional.of(existing));

        projector.onUnpublished("var-1", "web-1", Instant.parse("2026-01-01T00:00:00Z"));

        verify(offers, never()).save(any());
    }
}
