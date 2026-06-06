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
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UpdateBinCommandHandler implements CommandHandler<UpdateBinCommand, BinResult> {

    private final BinRepository binRepository;

    @Override
    @InventoryTransactional
    public BinResult handle(UpdateBinCommand command) {
        Bin bin = binRepository.findById(command.binId())
                .orElseThrow(() -> new InventoryServiceException(
                        new InventoryServiceError.BinNotFound(command.binId().getValue())));

        if (command.code() != null && !command.code().equals(bin.getCode())) {
            if (binRepository.existsByCodeAndZoneId(command.code(), bin.getZoneId())) {
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
