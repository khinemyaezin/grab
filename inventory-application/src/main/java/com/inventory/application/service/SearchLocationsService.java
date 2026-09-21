package com.inventory.application.service;

import com.inventory.application.port.inbound.SearchLocationsUseCase;

import com.grab.framework.id.IdGenerator;
import com.inventory.application.model.read.SearchLocationsQuery;
import com.inventory.application.model.read.SearchLocationsResult;
import com.inventory.application.port.outbound.LocationQueryPort;
import com.inventory.application.model.read.LocationSearchCriteria;
import com.inventory.application.model.read.LocationView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

@RequiredArgsConstructor
public class SearchLocationsService implements SearchLocationsUseCase {

    private final LocationQueryPort locationQueryPort;
    private final IdGenerator idGenerator;

            public Page<SearchLocationsResult> execute(SearchLocationsQuery query) {
        LocationSearchCriteria criteria = new LocationSearchCriteria(
                query.merchantId().getValue(),
                query.query(),
                query.type(),
                query.active()
        );
        return locationQueryPort.search(criteria, query.pageable())
                .map(this::toResult);
    }

        public Class<SearchLocationsQuery> getQueryType() {
        return SearchLocationsQuery.class;
    }

    private SearchLocationsResult toResult(LocationView view) {
        return new SearchLocationsResult(
                idGenerator.convertIdFrom(view.uuid()),
                view.code(),
                view.name(),
                view.type().name(),
                view.active(),
                new SearchLocationsResult.Address(
                        view.street(),
                        view.street2(),
                        view.city(),
                        view.state(),
                        view.postalCode(),
                        view.country()
                )
        );
    }
}
