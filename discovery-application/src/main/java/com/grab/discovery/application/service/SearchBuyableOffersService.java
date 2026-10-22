package com.grab.discovery.application.service;

import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.discovery.application.model.read.SearchBuyableOffersQuery;
import com.grab.discovery.application.port.inbound.SearchBuyableOffersUseCase;
import org.springframework.data.domain.Page;

public class SearchBuyableOffersService implements SearchBuyableOffersUseCase {

    @Override
    public Page<DiscoveryProductView> execute(SearchBuyableOffersQuery query) {
        return null;
    }
}
