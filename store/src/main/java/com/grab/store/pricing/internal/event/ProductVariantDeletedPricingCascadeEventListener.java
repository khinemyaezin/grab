package com.grab.store.pricing.internal.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.events.ProductVariantDeletedIntegrationEvent;
import com.grab.store.pricing.internal.command.DeletePriceSetForDeletedVariantCommand;
import com.grab.store.pricing.internal.config.PricingEnabled;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@PricingEnabled
@RequiredArgsConstructor
public class ProductVariantDeletedPricingCascadeEventListener {

    private static final Logger log = Loggers.getLogger(ProductVariantDeletedPricingCascadeEventListener.class);

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;

    @EventListener
    public void onVariantDeleted(ProductVariantDeletedIntegrationEvent event) {
        log.info(
                "Cascading price set delete for deleted variant: productId={}, variantId={}",
                event.productId(),
                event.variantId()
        );
        if (event.variantId() == null || event.variantId().isBlank()) {
            return;
        }
        Id variantId = idGenerator.convertIdFrom(event.variantId());
        commandBus.dispatch(new DeletePriceSetForDeletedVariantCommand(variantId));
    }
}
