package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.Bin;
import com.inventory.domain.repository.BinRepository;
import com.grab.store.inventory.internal.command.BinResult;
import com.grab.store.inventory.internal.command.UpdateBinCommand;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateBinCommandHandler implements CommandHandler<UpdateBinCommand, BinResult> {

    private final BinRepository binRepository;

    @Override
    @InventoryTransactional
    public BinResult handle(UpdateBinCommand command) {
        log.info("Updating bin with id={}", command.binId().getValue());
        
        Bin bin = binRepository.findById(command.binId())
                .orElseThrow(() -> {
                    log.warn("Bin not found: binId={}", command.binId().getValue());
                    return new InventoryServiceException(
                            new InventoryServiceError.BinNotFound(command.binId().getValue()));
                });

        if (command.code() != null && !command.code().equals(bin.getCode())) {
            if (binRepository.existsByCodeAndZoneId(command.code(), bin.getZoneId())) {
                log.warn("Bin code already exists: code={}, zoneId={}", command.code(), bin.getZoneId().getValue());
                throw new InventoryServiceException(
                        new InventoryServiceError.BinAlreadyExists(command.code()));
            }
        }

        bin.update(command.code(), command.name(), command.maxCapacity());

        if (command.active() != null) {
            if (command.active()) {
                bin.activate();
            } else {
                bin.deactivate();
            }
        }

        Bin saved = binRepository.save(bin);

        log.info("Updated bin with id={}, code={}", saved.getId().getValue(), saved.getCode());

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
    public Class<UpdateBinCommand> getCommandType() {
        return UpdateBinCommand.class;
    }
}
