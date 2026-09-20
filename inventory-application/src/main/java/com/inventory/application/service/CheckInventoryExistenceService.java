package com.inventory.application.service;

import com.inventory.application.port.inbound.CheckInventoryExistenceUseCase;

import com.grab.framework.id.IdGenerator;
import com.inventory.application.model.read.CheckInventoryExistenceQuery;
import com.inventory.application.model.read.CheckInventoryExistenceResult;
import com.inventory.application.port.outbound.InventoryQueryPort;
import com.inventory.application.model.read.InventoryExistenceView;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class CheckInventoryExistenceService implements CheckInventoryExistenceUseCase {

    private final InventoryQueryPort inventoryQueryPort;
    private final IdGenerator idGenerator;

            public CheckInventoryExistenceResult execute(CheckInventoryExistenceQuery query) {
        List<String> uniqueSkus = dedupeSkus(query.skus());
        String merchantId = query.merchantId().getValue();
        String locationId = query.locationId().getValue();

        List<InventoryExistenceView> existingViews = inventoryQueryPort.findExistenceByMerchantLocationAndSkus(
                merchantId,
                locationId,
                uniqueSkus
        );
        Map<String, String> inventoryIdsBySku = existingViews.stream()
                .collect(Collectors.toMap(
                        InventoryExistenceView::sku,
                        InventoryExistenceView::uuid,
                        (first, second) -> first
                ));

        List<CheckInventoryExistenceResult.Entry> items = new ArrayList<>(uniqueSkus.size());
        for (String sku : uniqueSkus) {
            String inventoryUuid = inventoryIdsBySku.get(sku);
            if (inventoryUuid == null) {
                items.add(new CheckInventoryExistenceResult.Entry(sku, false, null));
            } else {
                items.add(new CheckInventoryExistenceResult.Entry(
                        sku,
                        true,
                        idGenerator.convertIdFrom(inventoryUuid)
                ));
            }
        }
        return new CheckInventoryExistenceResult(items);
    }

        public Class<CheckInventoryExistenceQuery> getQueryType() {
        return CheckInventoryExistenceQuery.class;
    }

    private List<String> dedupeSkus(List<String> skus) {
        Set<String> unique = new LinkedHashSet<>(skus);
        return List.copyOf(unique);
    }
}
