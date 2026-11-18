package com.grab.service.checkout.cart.internal.adapter.outbound.message;

import com.cart.application.port.outbound.CatalogBuyabilityPort;
import com.cart.application.port.outbound.InventoryAvailabilityPort;
import com.cart.application.port.outbound.PricingQuotePort;
import com.cart.application.port.outbound.SalesChannelLookupPort;
import com.cart.domain.exception.CartDomainError;
import com.cart.domain.exception.CartDomainException;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.rpc.adapter.RmiClientFactory;
import com.grab.service.contract.message.channel.saleschannel.SalesChannelContextPort;
import com.grab.service.contract.message.merchandising.catalog.CatalogDiscoveryPort;

import java.rmi.RemoteException;
import java.util.Optional;

/** One instance per cart command, so stock is queried for the merchant of the validated variant. */
public final class CartPeerQueries
        implements SalesChannelLookupPort, CatalogBuyabilityPort, PricingQuotePort, InventoryAvailabilityPort {
    private static final String MERCHANDISING = "merchandising";

    private final RmiClientFactory clients;
    private final IdGenerator ids;
    private final int quantity;
    private final String regionId;
    private BuyableVariant variant;

    public CartPeerQueries(RmiClientFactory clients, IdGenerator ids, int quantity, String regionId) {
        this.clients = clients;
        this.ids = ids;
        this.quantity = quantity;
        this.regionId = regionId;
    }

    @Override
    public Optional<ChannelSnapshot> findEnabled(String salesChannelId) {
        try {
            var peer = clients.create(SalesChannelContextPort.class, "channel");
            Id channelId = ids.convertIdFrom(salesChannelId);
            var channel = peer.findChannelSlice(channelId);
            if (channel == null || !channel.enabled()) {
                return Optional.empty();
            }
            return Optional.of(new ChannelSnapshot(channel.salesChannelId(), channel.channelType()));
        } catch (RemoteException | IllegalArgumentException exception) {
            var error = new CartDomainError.ChannelDisabled(salesChannelId);
            throw peerFailure(error, "Sales channel lookup unavailable", exception);
        }
    }

    @Override
    public Optional<BuyableVariant> findPublished(String variantId, String salesChannelId) {
        try {
            var peer = clients.create(CatalogDiscoveryPort.class, MERCHANDISING);
            Id id = ids.convertIdFrom(variantId);
            Id channelId = ids.convertIdFrom(salesChannelId);
            var result = peer.findPublishedVariantSlice(id, channelId);
            if (result == null || !result.active()) {
                return Optional.empty();
            }
            variant = new BuyableVariant(result.variantId().getValue(), result.productId().getValue(),
                    result.merchantId().getValue(), result.sku(), result.title(), result.untracked());
            return Optional.of(variant);
        } catch (RemoteException | IllegalArgumentException exception) {
            var error = new CartDomainError.NotPublished(variantId, salesChannelId);
            throw peerFailure(error, "Catalog lookup unavailable", exception);
        }
    }

    @Override
    public Optional<QuotedUnitPrice> quote(String variantId, String currency, int total, String salesChannelId) {
        try {
            var peer = clients.create(
                    com.grab.service.contract.message.merchandising.pricing.PricingQuotePort.class, MERCHANDISING);
            Id id = ids.convertIdFrom(variantId);
            Id channelId = ids.convertIdFrom(salesChannelId);
            Id region = regionId == null || regionId.isBlank() ? null : ids.convertIdFrom(regionId);
            var quote = peer.quote(id, currency, total, channelId, region);
            return Optional.ofNullable(quote).map(result -> new QuotedUnitPrice(result.amount(), result.currencyCode()));
        } catch (RemoteException | IllegalArgumentException exception) {
            var error = new CartDomainError.NotPriced(variantId);
            throw peerFailure(error, "Pricing quote unavailable", exception);
        }
    }

    @Override
    public Availability available(String sku, String salesChannelId) {
        if (variant.untracked()) {
            return new Availability(0, true);
        }
        try {
            var peer = clients.create(
                    com.grab.service.contract.message.merchandising.inventory.InventoryAvailabilityPort.class,
                    MERCHANDISING);
            Id merchantId = ids.convertIdFrom(variant.sellerId());
            Id channelId = ids.convertIdFrom(salesChannelId);
            var stock = peer.findDiscoveryFacts(merchantId, sku, channelId);
            if (stock == null) {
                throw new RemoteException("Inventory returned no stock result");
            }
            return new Availability(stock.availableQty(), false);
        } catch (RemoteException | IllegalArgumentException exception) {
            var error = new CartDomainError.InsufficientStock(sku, quantity, 0);
            throw peerFailure(error, "Inventory availability lookup unavailable", exception);
        }
    }

    private CartDomainException peerFailure(CartDomainError error, String message, Exception cause) {
        var exception = new CartDomainException(error, message);
        exception.initCause(cause);
        return exception;
    }
}
