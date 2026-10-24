package com.grab.discovery.application.service;

import com.grab.discovery.application.model.read.DiscoveryProductView;
import com.grab.discovery.application.model.read.SearchBuyableOffersQuery;
import com.grab.discovery.application.port.inbound.SearchBuyableOffersUseCase;
import com.grab.discovery.application.port.outbound.DiscoveryProductQueryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

@RequiredArgsConstructor
public class SearchBuyableOffersService implements SearchBuyableOffersUseCase {
    private final DiscoveryProductQueryPort discoveryProductQueryPort;

    @Override
    public Page<DiscoveryProductView> execute(SearchBuyableOffersQuery query) {
        return discoveryProductQueryPort.searchVisible(
                query.salesChannelId(),
                query.keyword(),
                query.categoryId(),
                query.pageable()
        );
    }
}
