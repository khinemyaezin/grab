package com.inventory.application.service;

import lombok.RequiredArgsConstructor;

import com.inventory.application.port.inbound.GetInventoryReservationUseCase;

import com.grab.framework.id.IdGenerator;
import com.inventory.application.model.read.GetInventoryReservationsQuery;
import com.inventory.application.model.read.GetInventoryReservationsResult;
import com.inventory.application.port.outbound.InventoryReservationQueryPort;
import com.inventory.application.model.read.InventoryReservationView;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;

@RequiredArgsConstructor
public class GetInventoryReservationService implements GetInventoryReservationUseCase {
    private final InventoryReservationQueryPort inventoryReservationQueryPort;
    private final IdGenerator idGenerator;

            public Page<GetInventoryReservationsResult> execute(GetInventoryReservationsQuery query) {
        return inventoryReservationQueryPort.queryByInventoryItemId(
                query.inventoryItemId().getValue(), query.pageable())
                .map(this::toReservation);
    }

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
