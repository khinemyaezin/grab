package com.inventory.application.port.inbound;

import com.inventory.application.model.write.ExpireExpiredReservationsCommand;
import com.inventory.application.model.write.ExpireExpiredReservationsResult;

public interface ExpireExpiredReservationsUseCase {
    ExpireExpiredReservationsResult execute(ExpireExpiredReservationsCommand command);
}
