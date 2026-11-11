package com.grab.app.merchandising.pricing.internal.adapter.inbound.event;

import com.grab.app.shared.events.catalog.ProductVariantDeletedIntegrationEvent;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.pricing.application.model.write.DeletePriceSetForDeletedVariantCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ProductVariantDeletedPricingCascadeEventListener {

    private static final Logger log = Loggers.getLogger(ProductVariantDeletedPricingCascadeEventListener.class);

    private final CommandBus commandBus;
    private final IdGenerator idGenerator;

    public ProductVariantDeletedPricingCascadeEventListener(
            @Qualifier("merchandisingCommandBus") CommandBus commandBus,
            IdGenerator idGenerator
    ) {
        this.commandBus = commandBus;
        this.idGenerator = idGenerator;
    }

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
