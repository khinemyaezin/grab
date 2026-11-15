package com.grab.app.account.identity.internal.adapter.inbound.event;

import com.grab.app.shared.events.merchant.MerchantApplicationStartedIntegrationEvent;
import com.grab.app.shared.events.merchant.MerchantProfileUpdatedIntegrationEvent;
import com.grab.app.shared.events.merchant.StorefrontCreatedIntegrationEvent;
import com.grab.app.shared.events.merchant.StorefrontRenamedIntegrationEvent;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.id.Id;
import com.grab.framework.id.IdGenerator;
import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.outbox.infrastructure.inbox.AbstractIntegrationEventListener;
import com.identity.application.model.write.UpdateScopeDisplayCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
public class MerchantScopeDisplayProjectionListener extends AbstractIntegrationEventListener {
    private final CommandBus commandBus;
    private final IdGenerator ids;

    public MerchantScopeDisplayProjectionListener(
            @Qualifier("accountCommandBus") CommandBus commandBus,
            IdGenerator ids,
            @Qualifier("identityTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("identityProcessedEventRepository") ProcessedEventRepository processedEvents
    ) {
        super(transactionManager, processedEvents);
        this.commandBus = commandBus;
        this.ids = ids;
    }

    @EventListener
    public void onMerchantApplicationStarted(MerchantApplicationStartedIntegrationEvent event) {
        processDelivery("identity.project-merchant-scope-display", () -> {
            String merchantId = event.merchantId();
            String name = event.merchantName();
            updateDisplay(merchantId, name);
        });
    }

    @EventListener
    public void onMerchantProfileUpdated(MerchantProfileUpdatedIntegrationEvent event) {
        processDelivery("identity.project-merchant-scope-display", () -> {
            String merchantId = event.merchantId();
            String name = event.merchantName();
            updateDisplay(merchantId, name);
        });
    }

    @EventListener
    public void onStorefrontCreated(StorefrontCreatedIntegrationEvent event) {
        processDelivery("identity.project-merchant-scope-display", () -> {
            String storefrontId = event.storefrontId();
            String name = event.name();
            updateDisplay(storefrontId, name);
        });
    }

    @EventListener
    public void onStorefrontRenamed(StorefrontRenamedIntegrationEvent event) {
        processDelivery("identity.project-merchant-scope-display", () -> {
            String storefrontId = event.storefrontId();
            String name = event.name();
            updateDisplay(storefrontId, name);
        });
    }

    private void updateDisplay(String scopeIdValue, String name) {
        Id scopeId = ids.convertIdFrom(scopeIdValue);
        UpdateScopeDisplayCommand command = new UpdateScopeDisplayCommand(scopeId, name);
        commandBus.dispatch(command);
    }
}
