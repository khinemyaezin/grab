package com.grab.store.pricing.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.domain.Event;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.pricing.internal.command.CreateVariantPriceAssignmentCommand;
import com.grab.store.pricing.internal.command.CreateVariantPriceAssignmentResult;
import com.grab.store.pricing.internal.command.DeletePriceSetCommand;
import com.grab.store.pricing.internal.command.PriceRuleInput;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.workflows.events.PriceSetDeletedEvent;
import com.grab.store.workflows.events.RequestCreateVariantPriceEvent;
import com.grab.store.workflows.events.RequestDeletePriceSetCompensationEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.grab.store.workflows.events.VariantPriceCreatedEvent;
import com.pricing.infrastructure.workflow.PricingWorkflowStepRunner;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@PricingEnabled
@AllArgsConstructor
public class CreateSellableProductPricingEventListener {

    private static final Logger log = Loggers.getLogger(CreateSellableProductPricingEventListener.class);
    private static final int EVENT_VERSION = 1;
    private static final String STEP_CREATE_VARIANT_PRICES = "create-variant-prices";

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;
    private final PricingWorkflowStepRunner signalEmitter;

    @EventListener
    public void onRequestCreateVariantPrice(RequestCreateVariantPriceEvent event) {
        log.info(
                "Handling RequestCreateVariantPriceEvent workflowId={} variantId={} sku={}",
                event.workflowId(),
                event.variantId(),
                event.sku()
        );
        signalEmitter.runStep(
                event.workflowId(),
                () -> createVariantPrice(event),
                exception -> {
                    log.warn(
                            "Create variant price failed for workflowId={} variantId={}: {}",
                            event.workflowId(),
                            event.variantId(),
                            exception.getMessage()
                    );
                    return List.of(new SellableProductStepFailedEvent(
                            event.workflowId(),
                            STEP_CREATE_VARIANT_PRICES,
                            exception.getMessage(),
                            Instant.now(),
                            EVENT_VERSION
                    ));
                }
        );
    }

    @EventListener
    public void onRequestDeletePriceSetCompensation(RequestDeletePriceSetCompensationEvent event) {
        log.info(
                "Compensating price set delete workflowId={} priceSetId={}",
                event.workflowId(),
                event.priceSetId()
        );
        signalEmitter.runStep(
                event.workflowId(),
                () -> deletePriceSet(event),
                exception -> {
                    log.warn(
                            "Compensation delete price set failed workflowId={} priceSetId={}: {}",
                            event.workflowId(),
                            event.priceSetId(),
                            exception.getMessage()
                    );
                    return List.of();
                }
        );
    }

    private List<Event> createVariantPrice(RequestCreateVariantPriceEvent event) {
        List<PriceRuleInput> rules = event.rules().stream()
                .map(rule -> new PriceRuleInput(
                        rule.attribute(),
                        rule.value(),
                        rule.operator(),
                        rule.priority()
                ))
                .toList();
        CreateVariantPriceAssignmentResult result = commandBus.dispatch(
                new CreateVariantPriceAssignmentCommand(
                        event.variantId(),
                        event.productId(),
                        event.sku(),
                        event.merchantId(),
                        event.title(),
                        event.currencyCode(),
                        event.amount(),
                        event.minQuantity(),
                        event.maxQuantity(),
                        rules
                )
        );
        return List.of(new VariantPriceCreatedEvent(
                event.workflowId(),
                event.variantId(),
                event.sku(),
                result.priceSetId(),
                Instant.now(),
                EVENT_VERSION
        ));
    }

    private List<Event> deletePriceSet(RequestDeletePriceSetCompensationEvent event) {
        commandBus.dispatch(new DeletePriceSetCommand(idGenerator.convertIdFrom(event.priceSetId())));
        return List.of(new PriceSetDeletedEvent(
                event.workflowId(),
                event.priceSetId(),
                Instant.now(),
                EVENT_VERSION
        ));
    }
}
