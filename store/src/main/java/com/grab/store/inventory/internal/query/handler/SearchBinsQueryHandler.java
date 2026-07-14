package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.query.SearchBinsQuery;
import com.grab.store.inventory.internal.query.SearchBinsResult;
import com.inventory.infrastructure.repository.jpa.BinQueryRepository;
import com.inventory.infrastructure.specification.jpa.BinSearchCriteria;
import com.inventory.infrastructure.view.BinView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchBinsQueryHandler implements QueryHandler<SearchBinsQuery, Page<SearchBinsResult>> {

    private final BinQueryRepository binQueryRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryReadTransactional
    public Page<SearchBinsResult> handle(SearchBinsQuery query) {
        Id zoneId = query.zoneId();
        BinSearchCriteria criteria = new BinSearchCriteria(
                query.merchantId().getValue(),
                zoneId != null ? zoneId.getValue() : null,
                query.query(),
                query.active()
        );
        return binQueryRepository.search(criteria, query.pageable())
                .map(this::toResult);
    }

    @Override
    public Class<SearchBinsQuery> getQueryType() {
        return SearchBinsQuery.class;
    }

    private SearchBinsResult toResult(BinView view) {
        return new SearchBinsResult(
                idGenerator.convertIdFrom(view.uuid()),
                idGenerator.convertIdFrom(view.zoneId()),
                view.code(),
                view.name(),
                view.maxCapacity(),
                view.active()
        );
    }
}
