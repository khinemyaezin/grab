package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.query.GetInventoryMovementsQuery;
import com.grab.store.inventory.internal.query.GetInventoryMovementsResult;
import com.inventory.infrastructure.repository.jpa.StockMovementQueryRepository;
import com.inventory.infrastructure.view.StockMovementView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetInventoryMovementsQueryHandler implements QueryHandler<GetInventoryMovementsQuery, Page<GetInventoryMovementsResult>> {
    private final StockMovementQueryRepository stockMovementRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryReadTransactional
    public Page<GetInventoryMovementsResult> handle(GetInventoryMovementsQuery query) {
        return stockMovementRepository.queryByInventoryItemId(
                query.inventoryItemId().getValue(), query.pageable())
                .map(this::toMovement);
    }

    @Override
    public Class<GetInventoryMovementsQuery> getQueryType() {
        return GetInventoryMovementsQuery.class;
    }

    private GetInventoryMovementsResult toMovement(StockMovementView movement) {
        return new GetInventoryMovementsResult(
                idGenerator.convertIdFrom(movement.uuid()),
                idGenerator.convertIdFrom(movement.inventoryItemUuid()),
                movement.type().name(),
                movement.quantity(),
                movement.quantityBefore(),
                movement.quantityAfter(),
                movement.onHandBefore(),
                movement.onHandAfter(),
                movement.reservedBefore(),
                movement.reservedAfter(),
                movement.referenceId(),
                movement.createdAt()
        );
    }
}
