package com.grab.store.catalog.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.command.CreateProductSetCommand;
import com.grab.store.catalog.internal.command.CreateProductSetResult;
import com.grab.store.catalog.internal.command.DeleteProductCommand;
import com.grab.store.workflows.events.RequestCreateProductSetEvent;
import com.grab.store.workflows.events.RequestDeleteProductCompensationEvent;
import com.grab.store.workflows.events.SellableProductProductCreatedEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher events;

    @EventListener
    public void onRequestCreateProductSet(RequestCreateProductSetEvent event) {
        log.info("Handling RequestCreateProductSetEvent workflowId={}", event.workflowId());
        try {
            CreateProductSetCommand command = toCommand(event);
            CreateProductSetResult result = commandBus.dispatch(command);
            List<String> skus = extractSkus(event);
            events.publishEvent(new SellableProductProductCreatedEvent(
                    event.workflowId(),
                    result.productId(),
                    skus,
                    Instant.now(),
                    EVENT_VERSION
            ));
        } catch (RuntimeException exception) {
            log.warn("Create product set failed for workflowId={}: {}", event.workflowId(), exception.getMessage());
            events.publishEvent(new SellableProductStepFailedEvent(
                    event.workflowId(),
                    STEP_CREATE_PRODUCT,
                    exception.getMessage(),
                    Instant.now(),
                    EVENT_VERSION
            ));
        }
    }

    @EventListener
    public void onRequestDeleteProductCompensation(RequestDeleteProductCompensationEvent event) {
        log.info(
                "Compensating product delete workflowId={} productId={}",
                event.workflowId(),
                event.productId()
        );
        try {
            Id merchantId = idGenerator.convertIdFrom(event.merchantId());
            Id productId = idGenerator.convertIdFrom(event.productId());
            commandBus.dispatch(new DeleteProductCommand(merchantId, productId));
        } catch (RuntimeException exception) {
            log.warn(
                    "Compensation delete product failed workflowId={} productId={}: {}",
                    event.workflowId(),
                    event.productId(),
                    exception.getMessage()
            );
        }
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
                                .toList()
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
                        variants
                ),
                variantTypes
        );
    }

    private static List<String> extractSkus(RequestCreateProductSetEvent event) {
        if (event.product() == null || event.product().variants() == null) {
            return List.of();
        }
        return event.product().variants().stream()
                .map(RequestCreateProductSetEvent.Variant::sku)
                .filter(sku -> sku != null && !sku.isBlank())
                .toList();
    }
}
