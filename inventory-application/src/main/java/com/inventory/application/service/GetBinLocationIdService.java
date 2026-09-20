package com.inventory.application.service;

import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.application.port.inbound.GetBinLocationIdUseCase;
import com.inventory.application.port.outbound.BinQueryPort;
import com.inventory.application.port.outbound.ZoneQueryPort;
import com.inventory.application.model.read.GetBinLocationIdQuery;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetBinLocationIdService implements GetBinLocationIdUseCase {

    private final BinQueryPort binQueryPort;
    private final ZoneQueryPort zoneQueryPort;

    public String execute(GetBinLocationIdQuery query) {
        var bin = binQueryPort.findById(query.binId().getValue())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.BinNotFound(query.binId().getValue())));

        return zoneQueryPort.findById(bin.zoneId())
                .map(zone -> zone.locationId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.ZoneNotFound(bin.zoneId())));
    }

    public Class<GetBinLocationIdQuery> getQueryType() {
        return GetBinLocationIdQuery.class;
    }
}
