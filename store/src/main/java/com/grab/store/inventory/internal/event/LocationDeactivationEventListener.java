package com.grab.store.inventory.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.inventory.domain.aggregate.Zone;
import com.inventory.domain.event.LocationDeactivatedEvent;
import com.inventory.domain.repository.ZoneRepository;
import com.grab.store.inventory.internal.command.DeactivateZoneCommand;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.shared.security.PlatformScopes;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.context.event.EventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class LocationDeactivationEventListener {

    private static final Logger log = Loggers.getLogger(LocationDeactivationEventListener.class);

    private final ZoneRepository zoneRepository;
    private final CommandBus commandBus;

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
                        PlatformScopes.FULFILLMENT_LOCATION_SCOPE,
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
