package com.grab.store.catalog.internal.event;

import com.catalog.infrastructure.workflow.CatalogWorkflowStepRunner;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.command.UpdateProductCommand;
import com.grab.store.catalog.internal.command.UpdateProductResult;
import com.grab.store.workflows.events.RequestUpdateProductSetEvent;
import com.grab.store.workflows.events.SellableProductProductUpdatedEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class UpdateSellableProductCatalogEventListener {

    private static final Logger log = Loggers.getLogger(UpdateSellableProductCatalogEventListener.class);
    private static final int EVENT_VERSION = 1;
    private static final String STEP_UPDATE_PRODUCT = "update-product";

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;
    private final CatalogWorkflowStepRunner signalEmitter;

    @EventListener
    public void onRequestUpdateProductSet(RequestUpdateProductSetEvent event) {
        log.info("Handling RequestUpdateProductSetEvent workflowId={} productId={}", event.workflowId(), event.productId());
        signalEmitter.runStep(
                event.workflowId(),
                () -> updateProductSet(event),
                exception -> {
                    log.warn("Update product set failed for workflowId={}: {}", event.workflowId(), exception.getMessage());
                    return List.of(new SellableProductStepFailedEvent(
                            event.workflowId(),
                            STEP_UPDATE_PRODUCT,
                            exception.getMessage(),
                            Instant.now(),
                            EVENT_VERSION
                    ));
                }
        );
    }

    private List<Event> updateProductSet(RequestUpdateProductSetEvent event) {
        UpdateProductCommand command = toCommand(event);
        UpdateProductResult result = commandBus.dispatch(command);
        List<SellableProductProductUpdatedEvent.VariantRef> variants = result.variants().stream()
                .map(variant -> new SellableProductProductUpdatedEvent.VariantRef(
                        variant.variantId(),
                        variant.sku()
                ))
                .toList();
        List<String> skus = variants.stream()
                .map(SellableProductProductUpdatedEvent.VariantRef::sku)
                .filter(sku -> sku != null && !sku.isBlank())
                .toList();
        return List.of(new SellableProductProductUpdatedEvent(
                event.workflowId(),
                result.productId(),
                skus,
                variants,
                Instant.now(),
                EVENT_VERSION
        ));
    }

    private UpdateProductCommand toCommand(RequestUpdateProductSetEvent event) {
        Id merchantId = idGenerator.convertIdFrom(event.merchantId());
        Id productId = idGenerator.convertIdFrom(event.productId());
        Id categoryId = idGenerator.convertIdFrom(event.categoryId());
        UpdateProductCommand.VariantSync variantSync = toVariantSync(event.variantSync());
        return new UpdateProductCommand(
                merchantId,
                productId,
                event.name(),
                categoryId,
                event.condition(),
                event.slug(),
                variantSync,
                event.status()
        );
    }

    private UpdateProductCommand.VariantSync toVariantSync(RequestUpdateProductSetEvent.VariantSync variantSync) {
        if (variantSync == null) {
            return null;
        }
        UpdateProductCommand.VariantSyncIntent intent = variantSync.intent() == null
                ? UpdateProductCommand.VariantSyncIntent.LEAVE_AS_IS
                : UpdateProductCommand.VariantSyncIntent.valueOf(variantSync.intent().name());
        List<UpdateProductCommand.Variant> overrides = variantSync.overrides().stream()
                .map(variant -> new UpdateProductCommand.Variant(
                        variant.sku(),
                        variant.matrixKey(),
                        variant.variations() == null
                                ? List.of()
                                : variant.variations().stream()
                                .map(variation -> new UpdateProductCommand.Variation(
                                        idGenerator.convertIdFrom(variation.typeId()),
                                        idGenerator.convertIdFrom(variation.optionId())
                                ))
                                .toList(),
                        variant.manageInventory()
                ))
                .toList();
        List<UpdateProductCommand.VariantType> variantTypes = variantSync.variantTypes().stream()
                .map(type -> new UpdateProductCommand.VariantType(
                        idGenerator.convertIdFrom(type.typeId()),
                        type.options() == null
                                ? List.of()
                                : type.options().stream()
                                .map(option -> new UpdateProductCommand.VariantOption(
                                        idGenerator.convertIdFrom(option.optionId()),
                                        option.optionName()
                                ))
                                .toList()
                ))
                .toList();
        return new UpdateProductCommand.VariantSync(intent, overrides, variantTypes);
    }
}
