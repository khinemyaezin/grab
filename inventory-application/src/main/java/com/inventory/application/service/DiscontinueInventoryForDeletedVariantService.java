package com.inventory.application.service;

import com.inventory.application.port.inbound.DiscontinueInventoryForDeletedVariantUseCase;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.inventory.application.model.write.DiscontinueInventoryForDeletedVariantCommand;
import com.inventory.application.model.write.DiscontinueInventoryForDeletedVariantResult;
import com.inventory.domain.aggregate.InventoryItem;
import com.inventory.domain.policy.VariantDeletedInventoryDiscontinuePolicy;
import com.inventory.domain.port.outbound.InventoryRepository;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class DiscontinueInventoryForDeletedVariantService implements DiscontinueInventoryForDeletedVariantUseCase {

    private static final Logger log = Loggers.getLogger(DiscontinueInventoryForDeletedVariantService.class);

    private final InventoryRepository inventoryRepository;

            public DiscontinueInventoryForDeletedVariantResult execute(DiscontinueInventoryForDeletedVariantCommand command) {
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

        public Class<DiscontinueInventoryForDeletedVariantCommand> getCommandType() {
        return DiscontinueInventoryForDeletedVariantCommand.class;
    }
}
