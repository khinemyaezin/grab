package com.grab.service.channel.discovery.internal.adapter.outbound.message;

import com.grab.discovery.application.port.outbound.CatalogPort;
import com.grab.discovery.application.port.outbound.InventoryPort;
import com.grab.discovery.application.port.outbound.PricingPort;
import com.grab.framework.id.Id;
import com.grab.rpc.adapter.RmiClientFactory;
import com.grab.service.contract.message.merchandising.catalog.CatalogDiscoveryPort;
import com.grab.service.contract.message.merchandising.inventory.InventoryAvailabilityPort;
import com.grab.service.contract.message.merchandising.pricing.PricingQuotePort;
import org.springframework.stereotype.Component;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class MerchandisingDiscoveryAdapter implements CatalogPort, InventoryPort, PricingPort {
    private final RmiClientFactory clients;

    public MerchandisingDiscoveryAdapter(RmiClientFactory clients) {
        this.clients = clients;
    }

    @Override
    public Optional<ProductFacts> findProduct(Id productId) {
        var product = catalog(peer -> peer.findProductFacts(productId));
        return Optional.ofNullable(product).map(result -> {
            List<ProductOption> options = result.options().stream()
                    .map(option -> new ProductOption(option.name(), option.values())).toList();
            List<VariantFacts> variants = result.variants().stream().map(this::toVariant).toList();
            return new ProductFacts(result.productId(), result.merchantId(), result.categoryId(), result.title(),
                    result.slug(), result.description(), result.thumbnail(), result.status(), options, variants);
        });
    }

    @Override
    public Optional<VariantFacts> findVariant(Id variantId) {
        var variant = catalog(peer -> peer.findVariantFacts(variantId));
        return Optional.ofNullable(variant).map(this::toVariant);
    }

    @Override
    public List<ProductChannelPair> findPublishedProductChannelsAfter(Id productId, Id channelId, int limit) {
        var pairs = catalog(peer -> peer.findPublishedProductChannelsAfter(productId, channelId, limit));
        return toProductChannelPairs(pairs);
    }

    @Override
    public List<ProductChannelPair> findPublishedProductChannelsBySku(String sku) {
        var pairs = catalog(peer -> peer.findPublishedProductChannelsBySku(sku));
        return toProductChannelPairs(pairs);
    }

    @Override
    public List<ProductChannelPair> findPublishedProductChannelsForSalesChannelAfter(Id channelId, Id productId,
                                                                                    int limit) {
        var pairs = catalog(peer -> peer.findPublishedProductChannelsForSalesChannelAfter(channelId, productId, limit));
        return toProductChannelPairs(pairs);
    }

    @Override
    public boolean isPublishedInChannel(Id productId, Id channelId) {
        return catalog(peer -> peer.isPublishedInChannel(productId, channelId));
    }

    @Override
    public Availability available(String sku, Id merchantId, Id channelId) {
        var stock = invoke(InventoryAvailabilityPort.class, peer -> peer.findDiscoveryFacts(merchantId, sku, channelId));
        return new Availability(stock.availableQty(), stock.allowBackorder());
    }

    @Override
    public List<String> skusAtLocation(Id locationId) {
        throw new IllegalStateException("Location SKU refresh requires a Merchandising location lookup contract");
    }

    @Override
    public Optional<QuotedPrice> quote(Id variantId, String currency, int quantity, Id channelId, Id regionId) {
        var quote = invoke(PricingQuotePort.class, peer -> peer.quote(variantId, currency, quantity, channelId, regionId));
        return Optional.ofNullable(quote).map(this::toQuote);
    }

    @Override
    public Map<Id, QuotedPrice> quoteBatch(List<Id> variantIds, String currency, int quantity, Id channelId, Id regionId) {
        var request = new PricingQuotePort.BatchQuoteRequest(variantIds, currency, quantity, channelId, regionId);
        var batch = invoke(PricingQuotePort.class, peer -> peer.quoteBatch(request));
        Map<Id, QuotedPrice> quotes = new LinkedHashMap<>();
        for (Id variantId : variantIds) {
            var quote = batch.quotes().get(variantId);
            if (quote == null) {
                throw new IllegalStateException("Pricing batch response is missing a requested variant");
            }
            quotes.put(variantId, toQuote(quote));
        }
        return Map.copyOf(quotes);
    }

    private VariantFacts toVariant(CatalogDiscoveryPort.VariantFactsResult result) {
        return new VariantFacts(result.productId(), result.variantId(), result.sku(), result.title(), result.status(),
                result.manageInventory(), result.optionValues(), result.salesChannelIds());
    }

    private List<ProductChannelPair> toProductChannelPairs(List<CatalogDiscoveryPort.ProductChannelPairResult> pairs) {
        return pairs.stream().map(pair -> new ProductChannelPair(pair.productId(), pair.salesChannelId())).toList();
    }

    private QuotedPrice toQuote(PricingQuotePort.QuotedPriceResult quote) {
        return new QuotedPrice(quote.amount(), quote.originalAmount(), quote.currencyCode(), quote.priceSetId());
    }

    private <R> R catalog(PeerQuery<CatalogDiscoveryPort, R> query) {
        return invoke(CatalogDiscoveryPort.class, query);
    }

    private <T extends Remote, R> R invoke(Class<T> contract, PeerQuery<T, R> query) {
        try {
            T peer = clients.create(contract, "merchandising");
            return query.execute(peer);
        } catch (RemoteException exception) {
            throw new IllegalStateException("Merchandising lookup failed: " + contract.getSimpleName(), exception);
        }
    }

    @FunctionalInterface
    private interface PeerQuery<T, R> {
        R execute(T peer) throws RemoteException;
    }
}
