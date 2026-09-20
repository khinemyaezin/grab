package com.inventory.application.service;

import com.inventory.application.port.inbound.ListLocationsUseCase;

import com.grab.framework.id.IdGenerator;
import com.inventory.application.model.read.ListLocationsQuery;
import com.inventory.application.model.read.ListLocationsResult;
import com.inventory.application.port.outbound.LocationQueryPort;
import com.inventory.application.model.read.LocationView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

@RequiredArgsConstructor
public class ListLocationsService implements ListLocationsUseCase {
    private final LocationQueryPort locationRepository;
    private final IdGenerator idGenerator;

            public Page<ListLocationsResult> execute(ListLocationsQuery query) {
        return locationRepository.queryAll(query.merchantId().getValue(), query.pageable())
                .map(this::convertToLocation);
    }

        public Class<ListLocationsQuery> getQueryType() {
        return ListLocationsQuery.class;
    }

    private ListLocationsResult convertToLocation(LocationView locationView){
        return new ListLocationsResult(
                idGenerator.convertIdFrom(locationView.uuid()),
                locationView.code(),
                locationView.name(),
                locationView.type().name(),
                locationView.active(),
                new ListLocationsResult.Address(
                        locationView.street(),
                        locationView.street2(),
                        locationView.city(),
                        locationView.state(),
                        locationView.postalCode(),
                        locationView.country()
                )
        );
    }
}
