package com.grab.app.merchandising.inventory.internal.adapter.inbound.event;

import com.grab.app.shared.events.catalog.ProductDeletedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantDeletedIntegrationEvent;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.outbox.infrastructure.inbox.AbstractIntegrationEventListener;
import com.inventory.application.model.write.DiscontinueInventoryForDeletedVariantCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Component
public class ProductVariantDeletedInventoryCascadeEventListener extends AbstractIntegrationEventListener {
    private static final Logger log = Loggers.getLogger(ProductVariantDeletedInventoryCascadeEventListener.class);

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;

    public ProductVariantDeletedInventoryCascadeEventListener(
            @Qualifier("merchandisingCommandBus") CommandBus commandBus,
            IdGenerator idGenerator,
            @Qualifier("inventoryTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("inventoryProcessedEventRepository") ProcessedEventRepository processedEvents
    ) {
        super(transactionManager, processedEvents);
        this.commandBus = commandBus;
        this.idGenerator = idGenerator;
    }

    @EventListener
    public void onVariantDeleted(ProductVariantDeletedIntegrationEvent event) {
        processDelivery("inventory.discontinue-variant-inventory", () -> {
            log.info(
                    "Cascading inventory discontinue for deleted variant: productId={}, variantId={}",
                    event.productId(),
                    event.variantId()
            );
            dispatchDiscontinue(event.variantId());
        });
    }

    @EventListener
    public void onProductDeleted(ProductDeletedIntegrationEvent event) {
        processDelivery("inventory.discontinue-variant-inventory", () -> {
            List<String> variantIds = event.variantIds();
            if (variantIds == null || variantIds.isEmpty()) {
                log.info("Product deleted with no variant ids: productId={}", event.productId());
                return;
            }

            log.info(
                    "Cascading inventory discontinue for deleted product: productId={}, variantCount={}",
                    event.productId(),
                    variantIds.size()
            );
            for (String variantId : variantIds) {
                dispatchDiscontinue(variantId);
            }
        });
    }

    private void dispatchDiscontinue(String variantId) {
        if (variantId == null || variantId.isBlank()) {
            return;
        }
        Id productVariantId = idGenerator.convertIdFrom(variantId);
        DiscontinueInventoryForDeletedVariantCommand command =
                new DiscontinueInventoryForDeletedVariantCommand(productVariantId);
        commandBus.dispatch(command);
    }
}
