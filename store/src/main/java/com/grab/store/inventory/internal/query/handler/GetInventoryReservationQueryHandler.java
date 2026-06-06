package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.id.IdGenerator;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.query.GetInventoryReservationsQuery;
import com.grab.store.inventory.internal.query.GetInventoryReservationsResult;
import com.inventory.infrastructure.repository.jpa.InventoryReservationQueryRepository;
import com.inventory.infrastructure.view.InventoryReservationView;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class GetInventoryReservationQueryHandler implements QueryHandler<GetInventoryReservationsQuery, Page<GetInventoryReservationsResult>> {
    private final InventoryReservationQueryRepository inventoryReservationRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryReadTransactional
    public Page<GetInventoryReservationsResult> handle(GetInventoryReservationsQuery query) {
        return inventoryReservationRepository.queryByInventoryItemId(
                query.inventoryItemId().getValue(), query.pageable())
                .map(this::toReservation);
    }

    @Override
    public Class<GetInventoryReservationsQuery> getQueryType() {
        return GetInventoryReservationsQuery.class;
    }

    private GetInventoryReservationsResult toReservation(InventoryReservationView reservation) {
        return new GetInventoryReservationsResult(
                idGenerator.convertIdFrom(reservation.uuid()),
                idGenerator.convertIdFrom(reservation.inventoryItemUuid()),
                reservation.orderId(),
                reservation.orderLineId(),
                reservation.quantity(),
                reservation.status().name(),
                reservation.expiresAt(),
                reservation.idempotencyKey()
        );
    }
}
