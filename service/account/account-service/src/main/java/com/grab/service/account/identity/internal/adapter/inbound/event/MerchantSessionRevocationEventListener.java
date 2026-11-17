package com.grab.service.account.identity.internal.adapter.inbound.event;

import com.grab.service.contract.event.account.merchant.MerchantClosedIntegrationEvent;
import com.grab.service.contract.event.account.merchant.MerchantSuspendedIntegrationEvent;
import com.grab.service.contract.event.account.merchant.StorefrontStatusChangedIntegrationEvent;
import com.grab.framework.cqrs.command.CommandBus;
import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.outbox.infrastructure.inbox.AbstractIntegrationEventListener;
import com.identity.application.model.write.RevokeSessionsByScopeCommand;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
public class MerchantSessionRevocationEventListener extends AbstractIntegrationEventListener {
    public static final String MERCHANT_ACCOUNT_SCOPE = "merchant";
    public static final String MERCHANT_STOREFRONT_SCOPE = "storefront";

    private final CommandBus commandBus;

    public MerchantSessionRevocationEventListener(
            @Qualifier("accountCommandBus") CommandBus commandBus,
            @Qualifier("identityTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("identityProcessedEventRepository") ProcessedEventRepository processedEvents
    ) {
        super(transactionManager, processedEvents);
        this.commandBus = commandBus;
    }

    @EventListener
    public void onMerchantSuspended(MerchantSuspendedIntegrationEvent event) {
        processDelivery("identity.revoke-merchant-account-sessions", () -> {
            revokeMerchantAccount(event.merchantId());
        });
    }

    @EventListener
    public void onMerchantClosed(MerchantClosedIntegrationEvent event) {
        processDelivery("identity.revoke-merchant-account-sessions", () -> {
            revokeMerchantAccount(event.merchantId());
        });
    }

    @EventListener
    public void onStorefrontStatusChanged(StorefrontStatusChangedIntegrationEvent event) {
        processDelivery("identity.revoke-storefront-sessions", () -> {
            if (!"SUSPENDED".equals(event.status()) && !"CLOSED".equals(event.status())) {
                return;
            }
            var command = new RevokeSessionsByScopeCommand(
                    MERCHANT_STOREFRONT_SCOPE,
                    event.storefrontId()
            );
            commandBus.dispatch(command);
        });
    }

    private void revokeMerchantAccount(String merchantId) {
        var command = new RevokeSessionsByScopeCommand(
                MERCHANT_ACCOUNT_SCOPE,
                merchantId
        );
        commandBus.dispatch(command);
    }
}
