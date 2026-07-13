package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.domain.aggregate.Bin;
import com.inventory.domain.repository.BinRepository;
import com.inventory.domain.repository.ZoneRepository;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.query.GetBinLocationIdQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetBinLocationIdQueryHandler implements QueryHandler<GetBinLocationIdQuery, String> {

    private final BinRepository binRepository;
    private final ZoneRepository zoneRepository;

    @Override
    @InventoryReadTransactional
    public String handle(GetBinLocationIdQuery query) {
        Bin bin = binRepository.findById(query.binId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.BinNotFound(query.binId().getValue())));
        
        return zoneRepository.findById(bin.getZoneId())
                .map(zone -> zone.getLocationId().getValue())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.ZoneNotFound(bin.getZoneId().getValue())));
    }

    @Override
    public Class<GetBinLocationIdQuery> getQueryType() {
        return GetBinLocationIdQuery.class;
    }
}
