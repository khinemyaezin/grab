package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.domain.entity.StockMovement;
import com.inventory.domain.repository.StockMovementRepository;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.query.GetInventoryMovementsQuery;
import com.grab.store.inventory.internal.query.GetInventoryMovementsResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetInventoryMovementsQueryHandler
        implements QueryHandler<GetInventoryMovementsQuery, GetInventoryMovementsResult> {

    private final StockMovementRepository stockMovementRepository;

    @Override
    @InventoryReadTransactional
    public GetInventoryMovementsResult handle(GetInventoryMovementsQuery query) {
        List<StockMovement> movements = stockMovementRepository.findByInventoryItemId(query.inventoryItemId());
        return mapToResult(query.inventoryItemId().getValue(), movements);
    }

    @Override
    public Class<GetInventoryMovementsQuery> getQueryType() {
        return GetInventoryMovementsQuery.class;
    }

    private GetInventoryMovementsResult mapToResult(String inventoryItemId, List<StockMovement> movements) {
        List<GetInventoryMovementsResult.Movement> items = movements.stream()
                .map(movement -> new GetInventoryMovementsResult.Movement(
                        movement.getId().getValue(),
                        movement.getInventoryItemId().getValue(),
                        movement.getType().name(),
                        movement.getQuantity(),
                        movement.getQuantityBefore(),
                        movement.getQuantityAfter(),
                        movement.getOnHandBefore(),
                        movement.getOnHandAfter(),
                        movement.getReservedBefore(),
                        movement.getReservedAfter(),
                        movement.getReferenceId(),
                        movement.getCreatedAt()
                ))
                .toList();
        return new GetInventoryMovementsResult(inventoryItemId, items);
    }
}
