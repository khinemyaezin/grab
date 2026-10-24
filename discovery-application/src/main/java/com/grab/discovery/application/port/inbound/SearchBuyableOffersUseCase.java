package com.grab.discovery.application.port.inbound;

import com.grab.discovery.application.model.read.BuyableOfferResult;
import com.grab.discovery.application.model.read.SearchBuyableOffersQuery;
import org.springframework.data.domain.Page;

public interface SearchBuyableOffersUseCase {
    Page<BuyableOfferResult> execute(SearchBuyableOffersQuery query);
}
