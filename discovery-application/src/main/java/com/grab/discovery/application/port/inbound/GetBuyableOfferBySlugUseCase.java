package com.grab.discovery.application.port.inbound;

import com.grab.discovery.application.model.read.BuyableOfferResult;
import com.grab.discovery.application.model.read.GetBuyableOfferBySlugQuery;

public interface GetBuyableOfferBySlugUseCase {
    BuyableOfferResult execute(GetBuyableOfferBySlugQuery query);
}

