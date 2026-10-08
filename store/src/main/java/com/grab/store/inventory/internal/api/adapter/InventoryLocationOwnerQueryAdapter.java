package com.grab.store.inventory.internal.api.adapter;

import com.grab.framework.id.IdGenerator;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.shared.security.LocationOwnerQuery;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.application.model.read.GetLocationQuery;
import com.inventory.application.port.inbound.GetLocationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryLocationOwnerQueryAdapter implements LocationOwnerQuery {
    private final GetLocationUseCase getLocationUseCase;
    private final IdGenerator idGenerator;

    @Override
    @InventoryReadTransactional
    public boolean belongsToMerchant(String locationId, String merchantId) {
        var locationIdentifier = idGenerator.convertIdFrom(locationId);
        var query = new GetLocationQuery(locationIdentifier);
        try {
            var result = getLocationUseCase.execute(query);
            var actualMerchantId = result.merchantId().getValue();
            return merchantId.equals(actualMerchantId);
        } catch (InventoryServiceException exception) {
            return false;
        }
    }
}
