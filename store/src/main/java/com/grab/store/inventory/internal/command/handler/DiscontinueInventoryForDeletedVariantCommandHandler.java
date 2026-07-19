package com.grab.store.inventory.internal.command.handler;

import com.grab.framework.cqrs.command.CommandHandler;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.inventory.internal.command.DiscontinueInventoryForDeletedVariantCommand;
import com.grab.store.inventory.internal.command.DiscontinueInventoryForDeletedVariantResult;
import com.grab.store.inventory.internal.config.InventoryTransactional;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.policy.VariantDeletedInventoryDiscontinuePolicy;
import com.inventory.domain.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DiscontinueInventoryForDeletedVariantCommandHandler
        implements CommandHandler<DiscontinueInventoryForDeletedVariantCommand, DiscontinueInventoryForDeletedVariantResult> {

    private static final Logger log = Loggers.getLogger(DiscontinueInventoryForDeletedVariantCommandHandler.class);

    private final InventoryRepository inventoryRepository;

    @Override
    @InventoryTransactional
    public DiscontinueInventoryForDeletedVariantResult handle(DiscontinueInventoryForDeletedVariantCommand command) {
        String productVariantId = command.productVariantId().getValue();
        log.info("Discontinuing inventory items for deleted productVariantId={}", productVariantId);

        List<InventoryItem> items = inventoryRepository.findByProductVariantId(command.productVariantId());
        List<InventoryItem> toDiscontinue = VariantDeletedInventoryDiscontinuePolicy.selectForDiscontinue(items);
        int skippedCount = items.size() - toDiscontinue.size();

        for (InventoryItem item : toDiscontinue) {
            item.discontinue();
            inventoryRepository.save(item);
            log.info(
                    "Discontinued inventoryItemId={} sku={} locationId={} for deleted productVariantId={}",
                    item.getId().getValue(),
                    item.getSku(),
                    item.getLocationId().getValue(),
                    productVariantId
            );
        }

        log.info(
                "Completed variant-deleted discontinue for productVariantId={}, discontinued={}, skipped={}",
                productVariantId,
                toDiscontinue.size(),
                skippedCount
        );

        return new DiscontinueInventoryForDeletedVariantResult(
                productVariantId,
                toDiscontinue.size(),
                skippedCount
        );
    }

    @Override
    public Class<DiscontinueInventoryForDeletedVariantCommand> getCommandType() {
        return DiscontinueInventoryForDeletedVariantCommand.class;
    }
}
