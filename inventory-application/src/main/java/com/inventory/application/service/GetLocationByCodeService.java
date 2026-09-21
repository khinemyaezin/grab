package com.inventory.application.service;

import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.application.port.inbound.GetLocationByCodeUseCase;
import com.inventory.application.port.outbound.LocationQueryPort;
import com.inventory.application.model.read.GetLocationByCodeQuery;
import com.inventory.application.model.read.GetLocationResult;
import com.inventory.application.util.InventoryQueryResultMapper;
import com.grab.framework.id.IdGenerator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetLocationByCodeService implements GetLocationByCodeUseCase {

    private final LocationQueryPort locationQueryPort;
    private final IdGenerator idGenerator;

    public GetLocationResult execute(GetLocationByCodeQuery query) {
        return locationQueryPort.findByCode(query.code())
                .map(view -> InventoryQueryResultMapper.toLocationResult(view, idGenerator))
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.LocationNotFoundByCode(query.code())));
    }

    public Class<GetLocationByCodeQuery> getQueryType() {
        return GetLocationByCodeQuery.class;
    }
}
