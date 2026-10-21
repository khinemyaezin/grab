package com.grab.discovery.application.port.inbound;

import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.discovery.application.model.read.GetBuyableOfferBySlugQuery;

public interface GetBuyableOfferBySlugUseCase {
    DiscoveryProductView execute(GetBuyableOfferBySlugQuery query);
}

