package com.grab.store.inventory.internal.job;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.IdGenerator;
import com.grab.store.inventory.internal.command.ExpireExpiredReservationsCommand;
import com.grab.store.inventory.internal.command.ExpireExpiredReservationsResult;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class ReservationExpiryJob {

    private static final Logger log = Loggers.getLogger(ReservationExpiryJob.class);

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;

    @Value("${inventory.reservation.expiry.batch-size:100}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${inventory.reservation.expiry.fixed-delay-ms:60000}")
    public void expireReservations() {
        ExpireExpiredReservationsResult result = commandBus.dispatch(new ExpireExpiredReservationsCommand(
                LocalDateTime.now(),
                batchSize,
                idGenerator.convertIdFrom("system:reservation-expiry")
        ));
        if (result.scanned() > 0) {
            log.info("Reservation expiry job scanned={} expired={}", result.scanned(), result.expired());
        }
    }
}
