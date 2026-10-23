package com.grab.discovery.application.service;

import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.discovery.application.model.read.GetBuyableOfferBySlugQuery;
import com.grab.discovery.application.port.inbound.GetBuyableOfferBySlugUseCase;

public class GetBuyableOfferBySlugService implements GetBuyableOfferBySlugUseCase {

    @Override
    public DiscoveryProductView execute(GetBuyableOfferBySlugQuery query) {
        return null;
    }
}
