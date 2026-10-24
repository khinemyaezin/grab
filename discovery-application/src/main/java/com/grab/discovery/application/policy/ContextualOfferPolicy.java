package com.grab.discovery.application.policy;

import com.grab.discovery.application.model.read.BuyableOfferResult;
import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.discovery.application.model.read.ShoppingContext;
import com.grab.framework.id.Id;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContextualOfferPolicy {
    private static final String PROVIDER_CONTRACT_FAILURE = "Pricing provider contract failure.";
    private static final String ACTIVE = "ACTIVE";
    private static final String NOT_AVAILABLE_IN_REGION = "NOT_AVAILABLE_IN_REGION";
    private static final String PRICE_UNAVAILABLE = "PRICE_UNAVAILABLE";
    private static final String SOLD_OUT = "SOLD_OUT";
    private static final String AVAILABLE = "AVAILABLE";

    public BuyableOfferResult compose(
            DiscoveryProductView product,
            ShoppingContext context,
            boolean regionServed,
            Map<Id, Quote> quotedPrices
    ) {
        List<DiscoveryProductView.VariantFact> visibleVariants = visibleVariants(product.variants());
        Map<String, Quote> pricesByVariant = regionServed ? indexPrices(quotedPrices) : Map.of();
        List<BuyableOfferResult.VariantOffer> offers = new ArrayList<>();
        BigDecimal minimum = null;
        BigDecimal maximum = null;
        boolean productBuyable = false;

        for (DiscoveryProductView.VariantFact variant : visibleVariants) {
            Quote quote = resolveQuote(variant, regionServed, context, pricesByVariant);
            String availability = availability(variant, regionServed, quote);
            boolean buyable = AVAILABLE.equals(availability);
            String amount = amount(quote == null ? null : quote.amount());
            String originalAmount = quote == null || quote.amount() == null
                    ? null
                    : amount(quote.originalAmount());
            if (buyable) {
                productBuyable = true;
            }
            if (quote != null && quote.amount() != null) {
                minimum = minimum == null || quote.amount().compareTo(minimum) < 0 ? quote.amount() : minimum;
                maximum = maximum == null || quote.amount().compareTo(maximum) > 0 ? quote.amount() : maximum;
            }
            offers.add(new BuyableOfferResult.VariantOffer(
                    variant.variantId(),
                    variant.sku(),
                    variant.title(),
                    variant.status(),
                    variant.optionValues(),
                    variant.availableQty(),
                    variant.manageInventory(),
                    variant.allowBackorder(),
                    variant.inStock(),
                    availability,
                    buyable,
                    amount,
                    originalAmount
            ));
        }

        return new BuyableOfferResult(
                context.salesChannelId(),
                context.regionId(),
                context.countryCode(),
                context.currencyCode(),
                context.locale(),
                regionServed,
                product.productId(),
                product.merchantId(),
                product.categoryId(),
                product.title(),
                product.slug(),
                product.description(),
                product.thumbnail(),
                product.productStatus(),
                product.published(),
                product.channelEnabled(),
                product.hasStock(),
                productBuyable,
                amount(minimum),
                amount(maximum),
                product.options().stream()
                        .map(option -> new BuyableOfferResult.ProductOption(option.name(), option.values()))
                        .toList(),
                offers
        );
    }

    private List<DiscoveryProductView.VariantFact> visibleVariants(
            List<DiscoveryProductView.VariantFact> variants
    ) {
        return variants.stream()
                .filter(variant -> ACTIVE.equals(variant.status()))
                .toList();
    }

    private Map<String, Quote> indexPrices(Map<Id, Quote> quotedPrices) {
        Map<String, Quote> indexed = new HashMap<>();
        if (quotedPrices == null) {
            return indexed;
        }
        for (Map.Entry<Id, Quote> entry : quotedPrices.entrySet()) {
            if (entry.getKey() == null || entry.getKey().getValue() == null || entry.getValue() == null) {
                throw providerContractFailure();
            }
            indexed.put(entry.getKey().getValue(), entry.getValue());
        }
        return indexed;
    }

    private Quote resolveQuote(
            DiscoveryProductView.VariantFact variant,
            boolean regionServed,
            ShoppingContext context,
            Map<String, Quote> pricesByVariant
    ) {
        if (!regionServed) {
            return null;
        }
        if (variant.variantId() == null) {
            throw providerContractFailure();
        }
        if (!pricesByVariant.containsKey(variant.variantId().getValue())) {
            throw providerContractFailure();
        }
        Quote quote = pricesByVariant.get(variant.variantId().getValue());
        if (quote == null) {
            throw providerContractFailure();
        }
        if (quote.amount() != null
                && (context.currencyCode() == null
                || quote.currencyCode() == null
                || !context.currencyCode().equalsIgnoreCase(quote.currencyCode()))) {
            throw providerContractFailure();
        }
        return quote;
    }

    private IllegalStateException providerContractFailure() {
        return new IllegalStateException(PROVIDER_CONTRACT_FAILURE);
    }

    private String availability(
            DiscoveryProductView.VariantFact variant,
            boolean regionServed,
            Quote quote
    ) {
        if (!regionServed) {
            return NOT_AVAILABLE_IN_REGION;
        }
        if (quote == null || quote.amount() == null) {
            return PRICE_UNAVAILABLE;
        }
        if (variant.manageInventory() && variant.availableQty() <= 0 && !variant.allowBackorder()) {
            return SOLD_OUT;
        }
        return AVAILABLE;
    }

    private String amount(BigDecimal value) {
        return value == null ? null : value.toPlainString();
    }

    public record Quote(BigDecimal amount, BigDecimal originalAmount, String currencyCode) {
    }
}
