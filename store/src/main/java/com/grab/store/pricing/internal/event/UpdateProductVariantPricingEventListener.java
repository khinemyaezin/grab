package com.grab.store.pricing.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.pricing.internal.command.PriceRuleInput;
import com.grab.store.pricing.internal.command.UpdateVariantPriceCommand;
import com.grab.store.pricing.internal.command.UpdateVariantPriceResult;
import com.grab.store.pricing.internal.config.PricingEnabled;
import com.grab.store.workflows.events.RequestUpdateVariantPriceEvent;
import com.grab.store.workflows.events.SellableProductStepFailedEvent;
import com.grab.store.workflows.events.VariantPriceSyncedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class UpdateProductVariantPricingEventListener {

    private static final Logger log = Loggers.getLogger(UpdateProductVariantPricingEventListener.class);
    private static final int EVENT_VERSION = 1;
    private static final String STEP_UPDATE_VARIANT_PRICE = "update-variant-price";

    private final CommandBus commandBus;
    private final ApplicationEventPublisher events;

    @EventListener
    public void onRequestUpdateVariantPrice(RequestUpdateVariantPriceEvent event) {
        log.info(
                "Handling RequestUpdateVariantPriceEvent workflowId={} variantId={} sku={}",
                event.workflowId(),
                event.variantId(),
                event.sku()
        );
        try {
            List<PriceRuleInput> rules = event.rules().stream()
                    .map(rule -> new PriceRuleInput(
                            rule.attribute(),
                            rule.value(),
                            rule.operator(),
                            rule.priority()
                    ))
                    .toList();
            UpdateVariantPriceResult result = commandBus.dispatch(
                    new UpdateVariantPriceCommand(
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
            events.publishEvent(new VariantPriceSyncedEvent(
                    event.workflowId(),
                    event.variantId(),
                    event.sku(),
                    result.priceSetId(),
                    result.priceSetCreated(),
                    Instant.now(),
                    EVENT_VERSION
            ));
        } catch (RuntimeException exception) {
            log.warn(
                    "Update variant price failed for workflowId={} variantId={}: {}",
                    event.workflowId(),
                    event.variantId(),
                    exception.getMessage()
            );
            events.publishEvent(new SellableProductStepFailedEvent(
                    event.workflowId(),
                    STEP_UPDATE_VARIANT_PRICE,
                    exception.getMessage(),
                    Instant.now(),
                    EVENT_VERSION
            ));
        }
    }
}
