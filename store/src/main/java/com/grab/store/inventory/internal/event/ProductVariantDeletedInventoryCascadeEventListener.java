package com.grab.store.inventory.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.events.ProductDeletedIntegrationEvent;
import com.grab.store.catalog.events.ProductVariantDeletedIntegrationEvent;
import com.grab.store.inventory.internal.command.DiscontinueInventoryForDeletedVariantCommand;
import com.grab.store.inventory.internal.command.handler.DiscontinueInventoryForDeletedVariantCommandHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ProductVariantDeletedInventoryCascadeEventListener {
    private static final Logger log = Loggers.getLogger(ProductVariantDeletedInventoryCascadeEventListener.class);

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;

    @EventListener
    public void onVariantDeleted(ProductVariantDeletedIntegrationEvent event) {
        log.info(
                "Cascading inventory discontinue for deleted variant: productId={}, variantId={}",
                event.productId(),
                event.variantId()
        );
        dispatchDiscontinue(event.variantId());
    }

    @EventListener
    public void onProductDeleted(ProductDeletedIntegrationEvent event) {
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
