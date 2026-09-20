package com.inventory.application.service;

import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.application.port.inbound.GetBinUseCase;
import com.inventory.application.port.outbound.BinQueryPort;
import com.inventory.application.model.read.GetBinQuery;
import com.inventory.application.model.read.GetBinResult;
import com.inventory.application.util.InventoryQueryResultMapper;
import com.grab.framework.id.IdGenerator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetBinService implements GetBinUseCase {

    private final BinQueryPort binQueryPort;
    private final IdGenerator idGenerator;

    public GetBinResult execute(GetBinQuery query) {
        return binQueryPort.findById(query.binId().getValue())
                .map(view -> InventoryQueryResultMapper.toBinResult(view, idGenerator))
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.BinNotFound(query.binId().getValue())));
    }

    public Class<GetBinQuery> getQueryType() {
        return GetBinQuery.class;
    }
}
