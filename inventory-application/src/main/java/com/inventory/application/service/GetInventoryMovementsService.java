package com.inventory.application.service;

import com.inventory.application.port.inbound.GetInventoryMovementsUseCase;

import com.grab.framework.id.IdGenerator;
import com.inventory.application.model.read.GetInventoryMovementsQuery;
import com.inventory.application.model.read.GetInventoryMovementsResult;
import com.inventory.application.port.outbound.StockMovementQueryPort;
import com.inventory.application.model.read.StockMovementView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;

@RequiredArgsConstructor
public class GetInventoryMovementsService implements GetInventoryMovementsUseCase {
    private final StockMovementQueryPort stockMovementRepository;
    private final IdGenerator idGenerator;

            public Page<GetInventoryMovementsResult> execute(GetInventoryMovementsQuery query) {
        return stockMovementRepository.queryByInventoryItemId(
                query.inventoryItemId().getValue(), query.pageable())
                .map(this::toMovement);
    }

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
