package com.grab.store.merchant.internal.event;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.merchant.application.port.outbound.IdentityAccessManagementPort;
import com.merchant.application.security.MerchantAdminAccessProfile;
import com.merchant.domain.event.MerchantClosedEvent;
import com.merchant.domain.event.MerchantSuspendedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantLifecycleMemberSyncListener {
    private static final Logger log = Loggers.getLogger(MerchantLifecycleMemberSyncListener.class);

    private final IdentityAccessManagementPort identityAccessManagementPort;

    @EventListener
    public void onMerchantSuspended(MerchantSuspendedEvent event) {
        String merchantId = event.merchantId();
        log.warn("Merchant suspended merchantId={}. Revoking active merchant sessions.", merchantId);
        identityAccessManagementPort.revokeSessionsByScope(
                MerchantAdminAccessProfile.MERCHANT_SCOPE_KEY,
                merchantId
        );
    }

    @EventListener
    public void onMerchantClosed(MerchantClosedEvent event) {
        String merchantId = event.merchantId();
        log.warn("Merchant closed merchantId={}. Revoking active merchant sessions.", merchantId);
        identityAccessManagementPort.revokeSessionsByScope(
                MerchantAdminAccessProfile.MERCHANT_SCOPE_KEY,
                merchantId
        );
    }
}
