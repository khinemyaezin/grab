package com.grab.store.catalog.internal.command.handler;

import com.catalog.infrastructure.entity.entity.CatalogMerchantAvailabilityEntity;
import com.catalog.infrastructure.repository.jpa.CatalogMerchantAvailabilityJpaRepository;
import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.store.catalog.internal.command.UpsertMerchantAvailabilityCommand;
import com.grab.store.catalog.internal.config.CatalogTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
public class UpsertMerchantAvailabilityCommandHandler
        implements CommandHandler<UpsertMerchantAvailabilityCommand, Void> {

    private final CatalogMerchantAvailabilityJpaRepository availability;

    @Override
    @CatalogTransactional
    public Void handle(UpsertMerchantAvailabilityCommand command) {
        CatalogMerchantAvailabilityEntity entity = availability.findByMerchantId(command.merchantId())
                .orElseGet(CatalogMerchantAvailabilityEntity::new);
        entity.setMerchantId(command.merchantId());
        entity.setStatus(command.status());
        if (command.merchantType() != null && !command.merchantType().isBlank()) {
            entity.setMerchantType(command.merchantType());
        }
        entity.setUpdatedAt(Instant.now());
        availability.save(entity);
        return null;
    }

    @Override
    public Class<UpsertMerchantAvailabilityCommand> getCommandType() {
        return UpsertMerchantAvailabilityCommand.class;
    }
}
