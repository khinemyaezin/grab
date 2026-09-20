package com.grab.store.cart.internal.command.handler;

import com.cart.domain.aggregate.Cart;
import com.cart.domain.enums.CartStatus;
import com.cart.domain.exception.CartDomainError;
import com.cart.domain.exception.CartDomainException;
import com.cart.domain.repository.CartRepository;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
import com.grab.store.cart.internal.command.AddItemToCartCommand;
import com.grab.store.cart.internal.command.CartResult;
import com.grab.store.catalog.query.CatalogBuyabilityQueryPort;
import com.grab.store.inventory.query.InventoryAvailabilityQueryPort;
import com.grab.store.pricing.query.PricingQuoteQueryPort;
import com.grab.store.saleschannel.query.SalesChannelQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddItemToCartCommandHandlerTest {

    @Mock
    private SalesChannelQueryPort salesChannels;
    @Mock
    private CatalogBuyabilityQueryPort catalog;
    @Mock
    private PricingQuoteQueryPort pricing;
    @Mock
    private InventoryAvailabilityQueryPort inventory;

    private InMemoryCartRepository carts;
    private AddItemToCartCommandHandler handler;

    @BeforeEach
    void setUp() {
        carts = new InMemoryCartRepository();
        handler = new AddItemToCartCommandHandler(
                carts,
                salesChannels,
                catalog,
                pricing,
                inventory,
                new SequentialIdGenerator()
        );
    }

    @Test
    void addItem_shouldSnapshotLivePriceAndFlagAdjustment() {
        websiteEnabled();
        publishedVariant("var-1", "seller-1", "SKU-1");
        when(pricing.quote("var-1", "MMK", 1, "web-1"))
                .thenReturn(Optional.of(new PricingQuoteQueryPort.QuotedPrice(new BigDecimal("12000"), "MMK", "ps-1")));
        when(inventory.available("SKU-1", "web-1"))
                .thenReturn(new InventoryAvailabilityQueryPort.Availability(5, false));

        CartResult result = handler.handle(new AddItemToCartCommand(
                "guest-a",
                "web-1",
                "default",
                "MMK",
                "var-1",
                1,
                new BigDecimal("10000")
        ));

        assertThat(result.priceAdjusted()).isTrue();
        assertThat(result.lines()).hasSize(1);
        assertThat(result.lines().getFirst().unitPrice()).isEqualByComparingTo("12000");
        assertThat(result.lines().getFirst().quantity()).isEqualTo(1);
    }

    @Test
    void addItem_shouldAllowTwoGuestsToAddTheLastUnitWithoutReserving() {
        websiteEnabled();
        publishedVariant("var-1", "seller-1", "SKU-1");
        when(pricing.quote(eq("var-1"), eq("MMK"), eq(1), eq("web-1")))
                .thenReturn(Optional.of(new PricingQuoteQueryPort.QuotedPrice(new BigDecimal("10000"), "MMK", "ps-1")));
        when(inventory.available("SKU-1", "web-1"))
                .thenReturn(new InventoryAvailabilityQueryPort.Availability(1, false));

        CartResult first = handler.handle(add("guest-a", "var-1", 1));
        CartResult second = handler.handle(add("guest-b", "var-1", 1));

        assertThat(first.lines().getFirst().quantity()).isEqualTo(1);
        assertThat(second.lines().getFirst().quantity()).isEqualTo(1);
        assertThat(carts.savedCount()).isEqualTo(2);
    }

    @Test
    void addItem_shouldFailWhenUnpublished() {
        websiteEnabled();
        when(catalog.findVariant("var-1")).thenReturn(Optional.of(variant("var-1", "seller-1", "SKU-1")));
        when(catalog.isPublished("var-1", "web-1")).thenReturn(false);

        assertThatThrownBy(() -> handler.handle(add("guest-a", "var-1", 1)))
                .isInstanceOf(CartDomainException.class)
                .extracting(ex -> ((CartDomainException) ex).getMessageSource())
                .isInstanceOf(CartDomainError.NotPublished.class);
    }

    @Test
    void addItem_shouldRejectMixedSellerOnWebsite() {
        websiteEnabled();
        publishedVariant("var-1", "seller-1", "SKU-1");
        when(catalog.findVariant("var-2")).thenReturn(Optional.of(variant("var-2", "seller-2", "SKU-2")));
        when(catalog.isPublished("var-2", "web-1")).thenReturn(true);
        when(pricing.quote(eq("var-1"), eq("MMK"), eq(1), eq("web-1")))
                .thenReturn(Optional.of(new PricingQuoteQueryPort.QuotedPrice(new BigDecimal("10000"), "MMK", "ps-1")));
        when(pricing.quote(eq("var-2"), eq("MMK"), eq(1), eq("web-1")))
                .thenReturn(Optional.of(new PricingQuoteQueryPort.QuotedPrice(new BigDecimal("5000"), "MMK", "ps-2")));
        when(inventory.available(anyString(), eq("web-1")))
                .thenReturn(new InventoryAvailabilityQueryPort.Availability(5, false));

        handler.handle(add("guest-a", "var-1", 1));

        assertThatThrownBy(() -> handler.handle(add("guest-a", "var-2", 1)))
                .isInstanceOf(CartDomainException.class)
                .extracting(ex -> ((CartDomainException) ex).getMessageSource())
                .isInstanceOf(CartDomainError.MixedSeller.class);
    }

    @Test
    void addItem_shouldFailWhenLiveStockIsBelowRequestedTotal() {
        websiteEnabled();
        publishedVariant("var-1", "seller-1", "SKU-1");
        when(pricing.quote("var-1", "MMK", 1, "web-1"))
                .thenReturn(Optional.of(new PricingQuoteQueryPort.QuotedPrice(new BigDecimal("10000"), "MMK", "ps-1")));
        when(inventory.available("SKU-1", "web-1"))
                .thenReturn(new InventoryAvailabilityQueryPort.Availability(1, false));

        handler.handle(add("guest-a", "var-1", 1));
        when(pricing.quote("var-1", "MMK", 2, "web-1"))
                .thenReturn(Optional.of(new PricingQuoteQueryPort.QuotedPrice(new BigDecimal("10000"), "MMK", "ps-1")));

        assertThatThrownBy(() -> handler.handle(add("guest-a", "var-1", 1)))
                .isInstanceOf(CartDomainException.class)
                .extracting(ex -> ((CartDomainException) ex).getMessageSource())
                .isInstanceOf(CartDomainError.InsufficientStock.class);
    }

    private AddItemToCartCommand add(String guestToken, String variantId, int quantity) {
        return new AddItemToCartCommand(guestToken, "web-1", "default", "MMK", variantId, quantity, null);
    }

    private void websiteEnabled() {
        when(salesChannels.find("web-1")).thenReturn(Optional.of(
                new SalesChannelQueryPort.SalesChannelSlice("web-1", "WEBSITE", "ENABLED", "merchant-1")
        ));
    }

    private void publishedVariant(String variantId, String sellerId, String sku) {
        when(catalog.findVariant(variantId)).thenReturn(Optional.of(variant(variantId, sellerId, sku)));
        when(catalog.isPublished(variantId, "web-1")).thenReturn(true);
    }

    private CatalogBuyabilityQueryPort.CatalogVariantSlice variant(String variantId, String sellerId, String sku) {
        return new CatalogBuyabilityQueryPort.CatalogVariantSlice(
                variantId,
                "prod-1",
                sellerId,
                sku,
                "Item",
                "item",
                "ACTIVE",
                false,
                null
        );
    }

    private static final class InMemoryCartRepository implements CartRepository {
        private final Map<String, Cart> byId = new ConcurrentHashMap<>();
        private final AtomicInteger saves = new AtomicInteger();

        @Override
        public Optional<Cart> findById(Id id) {
            return Optional.ofNullable(byId.get(id.getValue()));
        }

        @Override
        public Optional<Cart> findOpenByGuestToken(String guestToken, Id salesChannelId, Id regionId, CartStatus status) {
            return byId.values().stream()
                    .filter(cart -> cart.getGuestToken().equals(guestToken))
                    .filter(cart -> cart.getSalesChannelId().equals(salesChannelId))
                    .filter(cart -> regionId == null || regionId.equals(cart.getRegionId()))
                    .filter(cart -> cart.getStatus() == status)
                    .findFirst();
        }

        @Override
        public Cart save(Cart cart) {
            saves.incrementAndGet();
            byId.put(cart.getId().getValue(), cart);
            return cart;
        }

        int savedCount() {
            return saves.get();
        }
    }

    private static final class SequentialIdGenerator implements IdGenerator {
        private final AtomicInteger sequence = new AtomicInteger();

        @Override
        public Id generateId() {
            return new CommonId("id-" + sequence.incrementAndGet());
        }

        @Override
        public Id convertIdFrom(String id) {
            return new CommonId(id);
        }
    }
}
