package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.id.IdGenerator;
import com.inventory.domain.aggregate.Bin;
import com.inventory.domain.aggregate.Zone;
import com.inventory.domain.repository.BinRepository;
import com.inventory.domain.repository.ZoneRepository;
import com.grab.store.inventory.internal.command.BinResult;
import com.grab.store.inventory.internal.command.CreateBinCommand;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.grab.store.inventory.internal.exception.InventoryServiceError;
import com.grab.store.inventory.internal.exception.InventoryServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateBinCommandHandler implements CommandHandler<CreateBinCommand, BinResult> {

    private final ZoneRepository zoneRepository;
    private final BinRepository binRepository;
    private final IdGenerator idGenerator;

    @Override
    @InventoryTransactional
    public BinResult handle(CreateBinCommand command) {
        log.info("Creating bin with code={} for zoneId={}", command.code(), command.zoneId().getValue());
        
        Zone zone = zoneRepository.findById(command.zoneId())
                .orElseThrow(() -> {
                    log.warn("Zone not found: zoneId={}", command.zoneId().getValue());
                    return new InventoryServiceException(
                            new InventoryServiceError.ZoneNotFound(command.zoneId().getValue()));
                });

        if (binRepository.existsByCodeAndZoneId(command.code(), command.zoneId())) {
            log.warn("Bin already exists with code={} in zoneId={}", command.code(), command.zoneId().getValue());
            throw new InventoryServiceException(
                    new InventoryServiceError.BinAlreadyExists(command.code()));
        }

        Bin bin = Bin.create(
                idGenerator.generateId(),
                command.zoneId(),
                command.code(),
                command.name(),
                command.maxCapacity()
        );

        Bin saved = binRepository.save(bin);

        log.info("Created bin with id={}, code={}, zoneId={}", saved.getId().getValue(), saved.getCode(), saved.getZoneId().getValue());

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
    public Class<CreateBinCommand> getCommandType() {
        return CreateBinCommand.class;
    }
}
