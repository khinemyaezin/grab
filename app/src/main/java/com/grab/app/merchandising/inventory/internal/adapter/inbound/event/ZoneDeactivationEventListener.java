package com.grab.app.merchandising.inventory.internal.adapter.inbound.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.inventory.application.model.write.DeactivateBinCommand;
import com.inventory.domain.aggregate.Bin;
import com.inventory.domain.event.ZoneDeactivatedEvent;
import com.inventory.domain.port.outbound.BinRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ZoneDeactivationEventListener {

    private static final Logger log = Loggers.getLogger(ZoneDeactivationEventListener.class);

    private final BinRepository binRepository;
    private final CommandBus commandBus;

    public ZoneDeactivationEventListener(
            BinRepository binRepository,
            @Qualifier("merchandisingCommandBus") CommandBus commandBus
    ) {
        this.binRepository = binRepository;
        this.commandBus = commandBus;
    }

    @EventListener
    public void handleZoneDeactivated(ZoneDeactivatedEvent event) {
        log.info("Handling ZoneDeactivatedEvent for zoneId={}", event.zoneId().getValue());

        List<Bin> activeBins = binRepository.findAllActiveByZoneId(event.zoneId());

        if (activeBins.isEmpty()) {
            log.info("No active bins found for zoneId={}", event.zoneId().getValue());
            return;
        }

        log.info("Found {} active bins to deactivate for zoneId={}", activeBins.size(), event.zoneId().getValue());

        for (Bin bin : activeBins) {
            try {
                DeactivateBinCommand command = new DeactivateBinCommand(
                        bin.getId(),
                        "system-cascade-deactivation",
                        "UNKNOWN",
                        "UNKNOWN"
                );
                commandBus.dispatch(command);
                log.info("Dispatched DeactivateBinCommand for binId={}", bin.getId().getValue());
            } catch (Exception e) {
                log.error("Failed to deactivate binId={} during zone deactivation cascade",
                        bin.getId().getValue(), e);
            }
        }
    }
}

