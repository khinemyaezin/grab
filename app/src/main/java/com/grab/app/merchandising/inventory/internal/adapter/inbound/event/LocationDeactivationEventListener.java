package com.grab.app.merchandising.inventory.internal.adapter.inbound.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.inventory.application.model.write.DeactivateZoneCommand;
import com.inventory.domain.aggregate.Zone;
import com.inventory.domain.event.LocationDeactivatedEvent;
import com.inventory.domain.port.outbound.ZoneRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocationDeactivationEventListener {

    private static final Logger log = Loggers.getLogger(LocationDeactivationEventListener.class);
    private static final String FULFILLMENT_LOCATION_SCOPE = "location";

    private final ZoneRepository zoneRepository;
    private final CommandBus commandBus;

    public LocationDeactivationEventListener(
            ZoneRepository zoneRepository,
            @Qualifier("merchandisingCommandBus") CommandBus commandBus
    ) {
        this.zoneRepository = zoneRepository;
        this.commandBus = commandBus;
    }

    @EventListener
    public void handleLocationDeactivated(LocationDeactivatedEvent event) {
        log.info("Handling LocationDeactivatedEvent for locationId={}", event.locationId().getValue());

        List<Zone> activeZones = zoneRepository.findAllActiveByLocationId(event.locationId());

        if (activeZones.isEmpty()) {
            log.info("No active zones found for locationId={}", event.locationId().getValue());
            return;
        }

        log.info("Found {} active zones to deactivate for locationId={}", activeZones.size(), event.locationId().getValue());

        for (Zone zone : activeZones) {
            try {
                DeactivateZoneCommand command = new DeactivateZoneCommand(
                        zone.getId(),
                        "system-cascade-deactivation",
                        FULFILLMENT_LOCATION_SCOPE,
                        event.locationId().getValue()
                );
                commandBus.dispatch(command);
                log.info("Dispatched DeactivateZoneCommand for zoneId={}", zone.getId().getValue());
            } catch (Exception e) {
                log.error("Failed to deactivate zoneId={} during location deactivation cascade",
                        zone.getId().getValue(), e);
            }
        }
    }
}

