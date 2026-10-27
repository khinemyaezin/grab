package com.grab.app.catalog.pricing.internal.adapter.inbound.event;

import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.app.shared.events.catalog.ProductVariantDeletedIntegrationEvent;
import com.pricing.application.model.write.DeletePriceSetForDeletedVariantCommand;
import com.grab.app.catalog.pricing.internal.config.PricingEnabled;
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
