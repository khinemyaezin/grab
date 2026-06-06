package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.Bin;
import com.inventory.domain.repository.BinRepository;
import com.grab.store.inventory.internal.command.BinResult;
import com.grab.store.inventory.internal.command.DeactivateBinCommand;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeactivateBinCommandHandler implements CommandHandler<DeactivateBinCommand, BinResult> {

    private final BinRepository binRepository;

    @Override
    @InventoryTransactional
    public BinResult handle(DeactivateBinCommand command) {
        Bin bin = binRepository.findById(command.binId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.BinNotFound(command.binId().getValue())));

        bin.deactivate();
        Bin saved = binRepository.save(bin);

        return new BinResult(
                saved.getId().getValue(),
                saved.getZoneId().getValue(),
                saved.getCode(),
                saved.getName(),
                saved.getMaxCapacity(),
                saved.isActive()
        );
    }

    @Override
    public Class<DeactivateBinCommand> getCommandType() {
        return DeactivateBinCommand.class;
    }
}
