package com.grab.store.cart.internal.adapter;

import com.cart.application.port.outbound.CatalogBuyabilityPort;
import com.grab.store.catalog.query.CatalogBuyabilityQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CatalogBuyabilityAdapter implements CatalogBuyabilityPort {
    private final CatalogBuyabilityQueryPort catalogBuyabilityQueryPort;

    @Override
    public Optional<BuyableVariant> findPublished(String variantId, String salesChannelId) {
        return catalogBuyabilityQueryPort.findPublished(variantId, salesChannelId)
                .map(slice -> new BuyableVariant(
                        slice.variantId(),
                        slice.productId(),
                        slice.sellerId(),
                        slice.sku(),
                        slice.title(),
                        slice.untracked()
                ));
    }
}
