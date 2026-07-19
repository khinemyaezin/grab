package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.query.CheckInventoryExistenceQuery;
import com.grab.store.inventory.internal.query.CheckInventoryExistenceResult;
import com.inventory.infrastructure.repository.jpa.InventoryQueryRepository;
import com.inventory.infrastructure.view.InventoryExistenceView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CheckInventoryExistenceQueryHandler
        implements QueryHandler<CheckInventoryExistenceQuery, CheckInventoryExistenceResult> {

    private final InventoryQueryRepository inventoryQueryRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryReadTransactional
    public CheckInventoryExistenceResult handle(CheckInventoryExistenceQuery query) {
        List<String> uniqueSkus = dedupeSkus(query.skus());
        String merchantId = query.merchantId().getValue();
        String locationId = query.locationId().getValue();

        List<InventoryExistenceView> existingViews = inventoryQueryRepository.findExistenceByMerchantLocationAndSkus(
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

    @Override
    public Class<CheckInventoryExistenceQuery> getQueryType() {
        return CheckInventoryExistenceQuery.class;
    }

    private List<String> dedupeSkus(List<String> skus) {
        Set<String> unique = new LinkedHashSet<>(skus);
        return List.copyOf(unique);
    }
}
