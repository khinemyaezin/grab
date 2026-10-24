package com.grab.discovery.application.service;

import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.discovery.application.model.read.GetBuyableOfferBySlugQuery;
import com.grab.discovery.application.exception.StorefrontQueryServiceError;
import com.grab.discovery.application.exception.StorefrontQueryServiceException;
import com.grab.discovery.application.port.inbound.GetBuyableOfferBySlugUseCase;
import com.grab.discovery.application.port.outbound.DiscoveryProductQueryPort;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetBuyableOfferBySlugService implements GetBuyableOfferBySlugUseCase {
    private final DiscoveryProductQueryPort discoveryProductQueryPort;

    @Override
    public DiscoveryProductView execute(GetBuyableOfferBySlugQuery query) {
        return discoveryProductQueryPort.findVisibleBySlug(query.salesChannelId(), query.slug())
                .orElseThrow(() -> new StorefrontQueryServiceException(
                        new StorefrontQueryServiceError.OfferNotFound(
                                query.slug(),
                                query.salesChannelId().getValue()
                        ),
                        "Offer not found."
                ));
    }
}
