package com.inventory.application.port.inbound;

import com.inventory.application.model.read.GetInventoryReservationsQuery;
import org.springframework.data.domain.Page;
import com.inventory.application.model.read.GetInventoryReservationsResult;

public interface GetInventoryReservationUseCase {
    Page<GetInventoryReservationsResult> execute(GetInventoryReservationsQuery query);
}
