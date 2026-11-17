package com.grab.service.account.merchant.internal.adapter.inbound.event;

import com.grab.framework.inbox.ProcessedEventRepository;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.outbox.infrastructure.inbox.AbstractIntegrationEventListener;
import com.merchant.application.port.outbound.IdentityAccessManagementPort;
import com.merchant.application.security.MerchantAdminAccessProfile;
import com.merchant.domain.event.MerchantClosedEvent;
import com.merchant.domain.event.MerchantSuspendedEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
public class MerchantLifecycleMemberSyncListener extends AbstractIntegrationEventListener {
    private static final Logger log = Loggers.getLogger(MerchantLifecycleMemberSyncListener.class);

    private final IdentityAccessManagementPort identityAccessManagementPort;

    public MerchantLifecycleMemberSyncListener(
            IdentityAccessManagementPort identityAccessManagementPort,
            @Qualifier("identityTransactionManager") PlatformTransactionManager transactionManager,
            @Qualifier("identityProcessedEventRepository") ProcessedEventRepository processedEvents
    ) {
        super(transactionManager, processedEvents);
        this.identityAccessManagementPort = identityAccessManagementPort;
    }

    @EventListener
    public void onMerchantSuspended(MerchantSuspendedEvent event) {
        processDelivery("identity.revoke-merchant-member-sessions", () -> {
            String merchantId = event.merchantId();
            log.warn("Merchant suspended merchantId={}. Revoking active merchant sessions.", merchantId);
            identityAccessManagementPort.revokeSessionsByScope(
                    MerchantAdminAccessProfile.MERCHANT_SCOPE_KEY,
                    merchantId
            );
        });
    }

    @EventListener
    public void onMerchantClosed(MerchantClosedEvent event) {
        processDelivery("identity.revoke-merchant-member-sessions", () -> {
            String merchantId = event.merchantId();
            log.warn("Merchant closed merchantId={}. Revoking active merchant sessions.", merchantId);
            identityAccessManagementPort.revokeSessionsByScope(
                    MerchantAdminAccessProfile.MERCHANT_SCOPE_KEY,
                    merchantId
            );
        });
    }
}
