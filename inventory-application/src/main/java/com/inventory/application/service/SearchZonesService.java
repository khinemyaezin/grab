package com.inventory.application.service;

import com.inventory.application.port.inbound.SearchZonesUseCase;

import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.inventory.application.model.read.SearchZonesQuery;
import com.inventory.application.model.read.SearchZonesResult;
import com.inventory.application.port.outbound.ZoneQueryPort;
import com.inventory.application.model.read.ZoneSearchCriteria;
import com.inventory.application.model.read.ZoneView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

@RequiredArgsConstructor
public class SearchZonesService implements SearchZonesUseCase {

    private final ZoneQueryPort zoneQueryPort;
    private final IdGenerator idGenerator;

            public Page<SearchZonesResult> execute(SearchZonesQuery query) {
        Id locationId = query.locationId();
        ZoneSearchCriteria criteria = new ZoneSearchCriteria(
                query.merchantId().getValue(),
                locationId != null ? locationId.getValue() : null,
                query.query(),
                query.type(),
                query.active()
        );
        return zoneQueryPort.search(criteria, query.pageable())
                .map(this::toResult);
    }

        public Class<SearchZonesQuery> getQueryType() {
        return SearchZonesQuery.class;
    }

    private SearchZonesResult toResult(ZoneView view) {
        return new SearchZonesResult(
                idGenerator.convertIdFrom(view.uuid()),
                idGenerator.convertIdFrom(view.locationId()),
                view.code(),
                view.name(),
                view.type().name(),
                view.active()
        );
    }
}
