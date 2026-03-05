package com.grab.store.inventory.internal.query.handler;

import com.grab.framework.cqrs.query.QueryHandler;
import com.inventory.domain.entity.InventoryReservation;
import com.inventory.domain.repository.InventoryReservationRepository;
import com.grab.store.inventory.internal.config.InventoryReadTransactional;
import com.grab.store.inventory.internal.query.GetInventoryReservationsQuery;
import com.grab.store.inventory.internal.query.GetInventoryReservationsResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetInventoryReservationsQueryHandler
        implements QueryHandler<GetInventoryReservationsQuery, GetInventoryReservationsResult> {

    private final InventoryReservationRepository inventoryReservationRepository;

    @Override
    @InventoryReadTransactional
    public GetInventoryReservationsResult handle(GetInventoryReservationsQuery query) {
        List<InventoryReservation> reservations = inventoryReservationRepository.findByInventoryItemId(query.inventoryItemId());
        return mapToResult(query.inventoryItemId().getValue(), reservations);
    }

    @Override
    public Class<GetInventoryReservationsQuery> getQueryType() {
        return GetInventoryReservationsQuery.class;
    }

    private GetInventoryReservationsResult mapToResult(
            String inventoryItemId,
            List<InventoryReservation> reservations
    ) {
        List<GetInventoryReservationsResult.Reservation> items = reservations.stream()
                .map(reservation -> new GetInventoryReservationsResult.Reservation(
                        reservation.getId().getValue(),
                        reservation.getInventoryItemId().getValue(),
                        reservation.getOrderId(),
                        reservation.getOrderLineId(),
                        reservation.getQuantity(),
                        reservation.getStatus().name(),
                        reservation.getExpiresAt(),
                        reservation.getIdempotencyKey()
                ))
                .toList();
        return new GetInventoryReservationsResult(inventoryItemId, items);
    }
}
