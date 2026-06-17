package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.inventory.domain.aggregate.Bin;
import com.inventory.domain.repository.BinRepository;
import com.grab.store.inventory.internal.command.DeleteBinCommand;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeleteBinCommandHandler implements CommandHandler<DeleteBinCommand, Void> {

    private final BinRepository binRepository;

    @Override
    @InventoryTransactional
    public Void handle(DeleteBinCommand command) {
        log.info("Deleting bin with id={}", command.binId().getValue());
        
        Bin bin = binRepository.findById(command.binId())
                .orElseThrow(() -> {
                    log.warn("Bin not found: binId={}", command.binId().getValue());
                    return new InventoryServiceException(
                            new InventoryServiceError.BinNotFound(command.binId().getValue()));
                });

        bin.delete();
        binRepository.delete(command.binId());

        log.info("Deleted bin with id={}, code={}", command.binId().getValue(), bin.getCode());

        return null;
    }

    @Override
    public Class<DeleteBinCommand> getCommandType() {
        return DeleteBinCommand.class;
    }
}
