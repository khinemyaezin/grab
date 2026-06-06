package com.grab.store.inventory.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.inventory.domain.aggregate.Zone;
import com.inventory.domain.event.LocationDeactivatedEvent;
import com.inventory.domain.repository.ZoneRepository;
import com.grab.store.inventory.internal.command.DeactivateZoneCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocationDeactivationEventListener {

    private final ZoneRepository zoneRepository;
    private final CommandBus commandBus;

    @TransactionalEventListener
    public void handleLocationDeactivated(LocationDeactivatedEvent event) {
        log.info("Handling LocationDeactivatedEvent for locationId={}", event.locationId().getValue());

        List<Zone> activeZones = List.of();//zoneRepository.findByLocationIdAndActive(event.locationId(), true);

        if (activeZones.isEmpty()) {
            log.info("No active zones found for locationId={}", event.locationId().getValue());
            return;
        }

        log.info("Found {} active zones to deactivate for locationId={}", activeZones.size(), event.locationId().getValue());

        for (Zone zone : activeZones) {
            try {
                DeactivateZoneCommand command = new DeactivateZoneCommand(
                        zone.getId(),
                        "system-cascade-deactivation"
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
