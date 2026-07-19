package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.Id;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.aggregate.Location;
import com.inventory.domain.repository.InventoryRepository;
import com.inventory.domain.repository.LocationRepository;
import com.grab.store.inventory.internal.command.DiscontinueInventoryCommand;
import com.grab.store.inventory.internal.command.InventoryItemResult;
import com.grab.store.inventory.internal.command.InventoryItemResults;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import com.grab.store.inventory.internal.policy.InventoryLocationAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DiscontinueInventoryCommandHandler implements CommandHandler<DiscontinueInventoryCommand, InventoryItemResult> {

    private final InventoryRepository inventoryRepository;
    private final LocationRepository locationRepository;
    private final InventoryLocationAccessPolicy locationAccessPolicy;

    @Override
    @InventoryTransactional
    public InventoryItemResult handle(DiscontinueInventoryCommand command) {
        InventoryItem item = requireAccessibleItem(command.inventoryItemId(), command.scopeKey(), command.scopeId());
        item.discontinue();
        inventoryRepository.save(item);
        return InventoryItemResults.from(item);
    }

    @Override
    public Class<DiscontinueInventoryCommand> getCommandType() {
        return DiscontinueInventoryCommand.class;
    }

    private InventoryItem requireAccessibleItem(Id inventoryItemId, String scopeKey, String scopeId) {
        InventoryItem item = inventoryRepository.findById(inventoryItemId)
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.InventoryNotFound(inventoryItemId.getValue())));
        Location location = locationRepository.findById(item.getLocationId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.LocationNotFound(item.getLocationId().getValue())));
        locationAccessPolicy.requireAccess(scopeKey, scopeId, location);
        return item;
    }
}
