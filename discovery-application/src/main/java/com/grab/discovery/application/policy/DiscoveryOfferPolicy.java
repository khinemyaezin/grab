package com.grab.discovery.application.policy;

import java.math.BigDecimal;
import java.util.Objects;

public class DiscoveryOfferPolicy {

    private static final String ACTIVE_STATUS = "ACTIVE";

    public boolean isOfferInStock(int availableQty, boolean untracked) {
        if (untracked) {
            return true;
        }
        return availableQty > 0;
    }

    public boolean isOfferBuyable(
            boolean published,
            String productStatus,
            boolean channelEnabled,
            BigDecimal price,
            String channelCurrency,
            String quotedCurrency,
            boolean inStock
    ) {
        boolean statusActive = ACTIVE_STATUS.equals(productStatus);
        boolean pricePresent = price != null;
        boolean currencyMatches = Objects.equals(channelCurrency, quotedCurrency);
        return published && statusActive && channelEnabled && pricePresent && currencyMatches && inStock;
    }

    public boolean isProductVisible(boolean channelEnabled, String productStatus, boolean anyVariantPublished) {
        boolean statusActive = ACTIVE_STATUS.equals(productStatus);
        return channelEnabled && statusActive && anyVariantPublished;
    }

    public boolean isProductActive(String productStatus) {
        return ACTIVE_STATUS.equals(productStatus);
    }
}
