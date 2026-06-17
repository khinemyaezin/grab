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
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeactivateBinCommandHandler implements CommandHandler<DeactivateBinCommand, BinResult> {

    private final BinRepository binRepository;

    @Override
    @InventoryTransactional
    public BinResult handle(DeactivateBinCommand command) {
        log.info("Deactivating bin with id={}", command.binId().getValue());
        
        Bin bin = binRepository.findById(command.binId())
                .orElseThrow(() -> {
                    log.warn("Bin not found: binId={}", command.binId().getValue());
                    return new InventoryServiceException(
                            new InventoryServiceError.BinNotFound(command.binId().getValue()));
                });

        bin.deactivate();
        Bin saved = binRepository.save(bin);

        log.info("Deactivated bin with id={}, code={}", saved.getId().getValue(), saved.getCode());

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
