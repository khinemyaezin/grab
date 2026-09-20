package com.inventory.application.service;

import com.inventory.application.exception.InventoryServiceError;
import com.inventory.application.exception.InventoryServiceException;
import com.inventory.application.port.inbound.GetInventoryUseCase;
import com.inventory.application.port.outbound.InventoryQueryPort;
import com.inventory.application.port.outbound.ProductVariantViewQueryPort;
import com.inventory.application.model.read.GetInventoryQuery;
import com.inventory.application.model.read.GetInventoryResult;
import com.inventory.application.model.read.ProductView;
import com.inventory.application.util.InventoryQueryResultMapper;
import com.grab.framework.id.IdGenerator;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GetInventoryService implements GetInventoryUseCase {

    private final InventoryQueryPort inventoryQueryPort;
    private final ProductVariantViewQueryPort productVariantViewQueryPort;
    private final IdGenerator idGenerator;

    public GetInventoryResult execute(GetInventoryQuery query) {
        var view = inventoryQueryPort.findById(query.inventoryItemId().getValue())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.InventoryNotFound(query.inventoryItemId().getValue())));
        String productName = resolveProductName(view.sku());
        return InventoryQueryResultMapper.toInventoryResult(view, productName, idGenerator);
    }

    public Class<GetInventoryQuery> getQueryType() {
        return GetInventoryQuery.class;
    }

    private String resolveProductName(String sku) {
        if (sku == null) {
            return null;
        }
        return productVariantViewQueryPort
                .findActiveBySkuContainingIgnoreCase(sku)
                .stream()
                .map(ProductView::getProductName)
                .findFirst()
                .orElse(null);
    }
}
