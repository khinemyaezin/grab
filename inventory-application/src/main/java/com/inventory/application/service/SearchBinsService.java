package com.inventory.application.service;

import com.inventory.application.port.inbound.SearchBinsUseCase;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.inventory.application.model.read.SearchBinsQuery;
import com.inventory.application.model.read.SearchBinsResult;
import com.inventory.application.port.outbound.BinQueryPort;
import com.inventory.application.model.read.BinSearchCriteria;
import com.inventory.application.model.read.BinView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

@RequiredArgsConstructor
public class SearchBinsService implements SearchBinsUseCase {

    private final BinQueryPort binQueryPort;
    private final IdGenerator idGenerator;

            public Page<SearchBinsResult> execute(SearchBinsQuery query) {
        Id zoneId = query.zoneId();
        BinSearchCriteria criteria = new BinSearchCriteria(
                query.merchantId().getValue(),
                zoneId != null ? zoneId.getValue() : null,
                query.query(),
                query.active()
        );
        return binQueryPort.search(criteria, query.pageable())
                .map(this::toResult);
    }

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
