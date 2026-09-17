package com.grab.store.catalog.internal.event;

import com.catalog.infrastructure.workflow.CatalogWorkflowStepRunner;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.command.UpdateVariantCommand;
import com.grab.store.catalog.internal.command.UpdateVariantResult;
import com.grab.store.workflows.events.RequestUpdateVariantEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.grab.store.workflows.events.VariantUpdatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UpdateProductVariantCatalogEventListener {

    private static final Logger log = Loggers.getLogger(UpdateProductVariantCatalogEventListener.class);
    private static final int EVENT_VERSION = 1;
    private static final String STEP_UPDATE_VARIANT = "update-variant";

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;
    private final CatalogWorkflowStepRunner signalEmitter;

    @EventListener
    public void onRequestUpdateVariant(RequestUpdateVariantEvent event) {
        log.info(
                "Handling RequestUpdateVariantEvent workflowId={} productId={} variantId={}",
                event.workflowId(),
                event.productId(),
                event.variantId()
        );
        signalEmitter.runStep(
                event.workflowId(),
                () -> updateVariant(event),
                exception -> {
                    log.warn("Update variant failed for workflowId={}: {}", event.workflowId(), exception.getMessage());
                    return List.of(new SellableProductStepFailedEvent(
                            event.workflowId(),
                            STEP_UPDATE_VARIANT,
                            exception.getMessage(),
                            Instant.now(),
                            EVENT_VERSION
                    ));
                }
        );
    }

    private List<Event> updateVariant(RequestUpdateVariantEvent event) {
        Id merchantId = idGenerator.convertIdFrom(event.merchantId());
        Id productId = idGenerator.convertIdFrom(event.productId());
        Id variantId = idGenerator.convertIdFrom(event.variantId());
        UpdateVariantCommand command = new UpdateVariantCommand(
                merchantId,
                productId,
                variantId,
                event.sku(),
                event.manageInventory()
        );
        UpdateVariantResult result = commandBus.dispatch(command);
        return List.of(new VariantUpdatedEvent(
                event.workflowId(),
                result.productId(),
                result.variantId(),
                result.sku(),
                Instant.now(),
                EVENT_VERSION
        ));
    }
}
