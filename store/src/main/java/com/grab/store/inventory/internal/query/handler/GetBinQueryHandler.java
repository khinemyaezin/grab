package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.domain.aggregate.Bin;
import com.inventory.domain.repository.BinRepository;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.query.GetBinQuery;
import com.grab.store.inventory.internal.query.GetBinResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetBinQueryHandler implements QueryHandler<GetBinQuery, GetBinResult> {

    private final BinRepository binRepository;

    @Override
    @InventoryReadTransactional
    public GetBinResult handle(GetBinQuery query) {
        Bin bin = binRepository.findById(query.binId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.BinNotFound(query.binId().getValue())));

        return new GetBinResult(
                bin.getId(),
                bin.getZoneId(),
                bin.getCode(),
                bin.getName(),
                bin.getMaxCapacity(),
                bin.isActive()
        );
    }

    @Override
    public Class<GetBinQuery> getQueryType() {
        return GetBinQuery.class;
    }
}
