package com.cart.application.service;

import com.cart.application.model.read.CartResult;
import com.cart.application.model.write.AddItemToCartCommand;
import com.cart.application.port.outbound.CatalogBuyabilityPort;
import com.cart.application.port.outbound.InventoryAvailabilityPort;
import com.cart.application.port.outbound.PricingQuotePort;
import com.cart.application.port.outbound.SalesChannelLookupPort;
import com.cart.domain.aggregate.Cart;
import com.cart.domain.enums.CartStatus;
import com.cart.domain.exception.CartDomainError;
import com.cart.domain.exception.CartDomainException;
import com.cart.domain.port.outbound.CartRepository;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.id.impl.CommonId;
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
class AddItemToCartServiceTest {

    @Mock
    private SalesChannelLookupPort salesChannels;
    @Mock
    private CatalogBuyabilityPort catalog;
    @Mock
    private PricingQuotePort pricing;
    @Mock
    private InventoryAvailabilityPort inventory;

    private InMemoryCartRepository carts;
    private AddItemToCartService service;

    @BeforeEach
    void setUp() {
        carts = new InMemoryCartRepository();
        service = new AddItemToCartService(
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
                .thenReturn(Optional.of(new PricingQuotePort.QuotedUnitPrice(new BigDecimal("12000"), "MMK")));
        when(inventory.available("SKU-1", "web-1"))
                .thenReturn(new InventoryAvailabilityPort.Availability(5, false));

        CartResult result = service.execute(new AddItemToCartCommand(
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
                .thenReturn(Optional.of(new PricingQuotePort.QuotedUnitPrice(new BigDecimal("10000"), "MMK")));
        when(inventory.available("SKU-1", "web-1"))
                .thenReturn(new InventoryAvailabilityPort.Availability(1, false));

        CartResult first = service.execute(add("guest-a", "var-1", 1));
        CartResult second = service.execute(add("guest-b", "var-1", 1));

        assertThat(first.lines().getFirst().quantity()).isEqualTo(1);
        assertThat(second.lines().getFirst().quantity()).isEqualTo(1);
        assertThat(carts.savedCount()).isEqualTo(2);
    }

    @Test
    void addItem_shouldFailWhenUnpublished() {
        websiteEnabled();
        when(catalog.findPublished("var-1", "web-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.execute(add("guest-a", "var-1", 1)))
                .isInstanceOf(CartDomainException.class)
                .extracting(ex -> ((CartDomainException) ex).getMessageSource())
                .isInstanceOf(CartDomainError.NotPublished.class);
    }

    @Test
    void addItem_shouldRejectMixedSellerOnWebsite() {
        websiteEnabled();
        publishedVariant("var-1", "seller-1", "SKU-1");
        when(catalog.findPublished("var-2", "web-1"))
                .thenReturn(Optional.of(buyableVariant("var-2", "seller-2", "SKU-2")));
        when(pricing.quote(eq("var-1"), eq("MMK"), eq(1), eq("web-1")))
                .thenReturn(Optional.of(new PricingQuotePort.QuotedUnitPrice(new BigDecimal("10000"), "MMK")));
        when(pricing.quote(eq("var-2"), eq("MMK"), eq(1), eq("web-1")))
                .thenReturn(Optional.of(new PricingQuotePort.QuotedUnitPrice(new BigDecimal("5000"), "MMK")));
        when(inventory.available(anyString(), eq("web-1")))
                .thenReturn(new InventoryAvailabilityPort.Availability(5, false));

        service.execute(add("guest-a", "var-1", 1));

        assertThatThrownBy(() -> service.execute(add("guest-a", "var-2", 1)))
                .isInstanceOf(CartDomainException.class)
                .extracting(ex -> ((CartDomainException) ex).getMessageSource())
                .isInstanceOf(CartDomainError.MixedSeller.class);
    }

    @Test
    void addItem_shouldFailWhenLiveStockIsBelowRequestedTotal() {
        websiteEnabled();
        publishedVariant("var-1", "seller-1", "SKU-1");
        when(pricing.quote("var-1", "MMK", 1, "web-1"))
                .thenReturn(Optional.of(new PricingQuotePort.QuotedUnitPrice(new BigDecimal("10000"), "MMK")));
        when(inventory.available("SKU-1", "web-1"))
                .thenReturn(new InventoryAvailabilityPort.Availability(1, false));

        service.execute(add("guest-a", "var-1", 1));
        when(pricing.quote("var-1", "MMK", 2, "web-1"))
                .thenReturn(Optional.of(new PricingQuotePort.QuotedUnitPrice(new BigDecimal("10000"), "MMK")));

        assertThatThrownBy(() -> service.execute(add("guest-a", "var-1", 1)))
                .isInstanceOf(CartDomainException.class)
                .extracting(ex -> ((CartDomainException) ex).getMessageSource())
                .isInstanceOf(CartDomainError.InsufficientStock.class);
    }

    private AddItemToCartCommand add(String guestToken, String variantId, int quantity) {
        return new AddItemToCartCommand(guestToken, "web-1", "default", "MMK", variantId, quantity, null);
    }

    private void websiteEnabled() {
        when(salesChannels.findEnabled("web-1")).thenReturn(Optional.of(
                new SalesChannelLookupPort.ChannelSnapshot("web-1", "WEBSITE")
        ));
    }

    private void publishedVariant(String variantId, String sellerId, String sku) {
        when(catalog.findPublished(variantId, "web-1")).thenReturn(Optional.of(buyableVariant(variantId, sellerId, sku)));
    }

    private CatalogBuyabilityPort.BuyableVariant buyableVariant(String variantId, String sellerId, String sku) {
        return new CatalogBuyabilityPort.BuyableVariant(
                variantId,
                "prod-1",
                sellerId,
                sku,
                "Item",
                false
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
