package com.grab.store.catalog.internal.event;

import com.catalog.adapter.persistence.workflow.CatalogWorkflowStepRunner;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.domain.Event;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.catalog.application.command.CreateProductSetCommand;
import com.catalog.application.command.CreateProductSetResult;
import com.catalog.application.command.DeleteProductCommand;
import com.grab.store.workflows.events.ProductDeletedEvent;
import com.grab.store.workflows.events.RequestCreateProductSetEvent;
import com.grab.store.workflows.events.RequestDeleteProductCompensationEvent;
import com.grab.store.workflows.events.SellableProductProductCreatedEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CreateSellableProductCatalogEventListener {

    private static final Logger log = Loggers.getLogger(CreateSellableProductCatalogEventListener.class);
    private static final int EVENT_VERSION = 1;
    private static final String STEP_CREATE_PRODUCT = "create-product";

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;
    private final CatalogWorkflowStepRunner signalEmitter;

    @EventListener
    public void onRequestCreateProductSet(RequestCreateProductSetEvent event) {
        log.info("Handling RequestCreateProductSetEvent workflowId={}", event.workflowId());
        signalEmitter.runStep(
                event.workflowId(),
                () -> createProductSet(event),
                exception -> {
                    log.warn(
                            "Create product set failed for workflowId={}: {}",
                            event.workflowId(),
                            exception.getMessage()
                    );
                    return List.of(new SellableProductStepFailedEvent(
                            event.workflowId(),
                            STEP_CREATE_PRODUCT,
                            exception.getMessage(),
                            Instant.now(),
                            EVENT_VERSION
                    ));
                }
        );
    }

    @EventListener
    public void onRequestDeleteProductCompensation(RequestDeleteProductCompensationEvent event) {
        log.info(
                "Compensating product delete workflowId={} productId={}",
                event.workflowId(),
                event.productId()
        );
        signalEmitter.runStep(
                event.workflowId(),
                () -> deleteProduct(event),
                exception -> {
                    log.warn(
                            "Compensation delete product failed workflowId={} productId={}: {}",
                            event.workflowId(),
                            event.productId(),
                            exception.getMessage()
                    );
                    return List.of();
                }
        );
    }

    private List<Event> createProductSet(RequestCreateProductSetEvent event) {
        CreateProductSetResult result = commandBus.dispatch(toCommand(event));
        List<SellableProductProductCreatedEvent.VariantRef> variants = result.variants().stream()
                .map(variant -> new SellableProductProductCreatedEvent.VariantRef(
                        variant.variantId(),
                        variant.sku()
                ))
                .toList();
        List<String> skus = variants.stream()
                .map(SellableProductProductCreatedEvent.VariantRef::sku)
                .filter(sku -> sku != null && !sku.isBlank())
                .toList();
        return List.of(new SellableProductProductCreatedEvent(
                event.workflowId(),
                result.productId(),
                skus,
                variants,
                result.status(),
                Instant.now(),
                EVENT_VERSION
        ));
    }

    private List<Event> deleteProduct(RequestDeleteProductCompensationEvent event) {
        Id merchantId = idGenerator.convertIdFrom(event.merchantId());
        Id productId = idGenerator.convertIdFrom(event.productId());
        commandBus.dispatch(new DeleteProductCommand(merchantId, productId));
        return List.of(new ProductDeletedEvent(
                event.workflowId(),
                event.productId(),
                Instant.now(),
                EVENT_VERSION
        ));
    }

    private CreateProductSetCommand toCommand(RequestCreateProductSetEvent event) {
        Id merchantId = idGenerator.convertIdFrom(event.merchantId());
        RequestCreateProductSetEvent.Product product = event.product();
        List<CreateProductSetCommand.Variant> variants = product.variants() == null
                ? List.of()
                : product.variants().stream()
                .map(variant -> new CreateProductSetCommand.Variant(
                        variant.sku(),
                        variant.variations() == null
                                ? List.of()
                                : variant.variations().stream()
                                .map(variation -> new CreateProductSetCommand.Variation(
                                        idGenerator.convertIdFrom(variation.optionId()),
                                        idGenerator.convertIdFrom(variation.typeId())
                                ))
                                .toList(),
                        variant.manageInventory()
                ))
                .toList();
        List<CreateProductSetCommand.VariantType> variantTypes = event.variantTypes() == null
                ? List.of()
                : event.variantTypes().stream()
                .map(type -> new CreateProductSetCommand.VariantType(
                        type.typeId(),
                        type.options() == null
                                ? List.of()
                                : type.options().stream()
                                .map(option -> new CreateProductSetCommand.VariantOption(option.optionId()))
                                .toList()
                ))
                .toList();
        return new CreateProductSetCommand(
                merchantId,
                new CreateProductSetCommand.Product(
                        product.name(),
                        idGenerator.convertIdFrom(product.categoryId()),
                        product.condition(),
                        product.slug(),
                        product.status(),
                        variants
                ),
                variantTypes
        );
    }
}
