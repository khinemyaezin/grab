package com.grab.discovery.application.port.inbound;

import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.discovery.application.model.read.SearchBuyableOffersQuery;
import org.springframework.data.domain.Page;

public interface SearchBuyableOffersUseCase {
    Page<DiscoveryProductView> execute(SearchBuyableOffersQuery query);
}
