package com.grab.app.merchandising.inventory.internal.adapter.inbound.event;

import com.grab.app.shared.events.catalog.ProductDeletedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductPresentationChangedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantAddedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantDeletedIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantRestoredIntegrationEvent;
import com.grab.app.shared.events.catalog.ProductVariantUpdatedIntegrationEvent;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.outbox.infrastructure.inbox.AbstractIntegrationEventListener;
import com.inventory.application.model.write.ProjectProductVariantViewCommand.DeleteProduct;
import com.inventory.application.model.write.ProjectProductVariantViewCommand.RenameProduct;
import com.inventory.application.model.write.ProjectProductVariantViewCommand.SetVariantStatus;
import com.inventory.application.model.write.ProjectProductVariantViewCommand.Update;
import com.inventory.application.model.write.ProjectProductVariantViewCommand.UpsertVariant;
import com.inventory.application.model.write.ProjectProductVariantViewCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
public class ProductVariantViewProjectionEventListener extends AbstractIntegrationEventListener {
    private final CommandBus commandBus;
    private final IdGenerator idGenerator;

    public ProductVariantViewProjectionEventListener(
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
    public void onVariantAdded(ProductVariantAddedIntegrationEvent event) {
        processDelivery("inventory.project-product-variant-view", () -> {
            Id productId = idGenerator.convertIdFrom(event.productId());
            Id variantId = idGenerator.convertIdFrom(event.variantId());
            UpsertVariant update = new UpsertVariant(
                    productId, variantId, event.sku(), event.productName(), event.manageInventory(), true
            );
            dispatch(update);
        });
    }

    @EventListener
    public void onVariantUpdated(ProductVariantUpdatedIntegrationEvent event) {
        processDelivery("inventory.project-product-variant-view", () -> {
            Id productId = idGenerator.convertIdFrom(event.productId());
            Id variantId = idGenerator.convertIdFrom(event.variantId());
            UpsertVariant update = new UpsertVariant(
                    productId, variantId, event.sku(), null, event.manageInventory(), false
            );
            dispatch(update);
        });
    }

    @EventListener
    public void onVariantDeleted(ProductVariantDeletedIntegrationEvent event) {
        processDelivery("inventory.project-product-variant-view", () -> {
            Id variantId = idGenerator.convertIdFrom(event.variantId());
            SetVariantStatus update = new SetVariantStatus(variantId, false);
            dispatch(update);
        });
    }

    @EventListener
    public void onVariantRestored(ProductVariantRestoredIntegrationEvent event) {
        processDelivery("inventory.project-product-variant-view", () -> {
            Id variantId = idGenerator.convertIdFrom(event.variantId());
            SetVariantStatus update = new SetVariantStatus(variantId, true);
            dispatch(update);
        });
    }

    @EventListener
    public void onProductPresentationChanged(ProductPresentationChangedIntegrationEvent event) {
        processDelivery("inventory.project-product-variant-view", () -> {
            Id productId = idGenerator.convertIdFrom(event.productId());
            RenameProduct update = new RenameProduct(productId, event.productName());
            dispatch(update);
        });
    }

    @EventListener
    public void onProductDeleted(ProductDeletedIntegrationEvent event) {
        processDelivery("inventory.project-product-variant-view", () -> {
            Id productId = idGenerator.convertIdFrom(event.productId());
            DeleteProduct update = new DeleteProduct(productId);
            dispatch(update);
        });
    }

    private void dispatch(Update update) {
        ProjectProductVariantViewCommand command = new ProjectProductVariantViewCommand(update);
        commandBus.dispatch(command);
    }
}
