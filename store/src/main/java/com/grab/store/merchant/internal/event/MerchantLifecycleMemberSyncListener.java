package com.grab.store.merchant.internal.event;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.identity.port.AccessManagementPort;
import com.merchant.domain.event.MerchantAccessProfile;
import com.merchant.domain.event.MerchantClosedEvent;
import com.merchant.domain.event.MerchantSuspendedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantLifecycleMemberSyncListener {
    private static final Logger log = Loggers.getLogger(MerchantLifecycleMemberSyncListener.class);

    private final AccessManagementPort accessManagementPort;

    @EventListener
    public void onMerchantSuspended(MerchantSuspendedEvent event) {
        log.warn("Merchant suspended merchantId={}. Revoking active seller portal sessions.", event.merchantId());
        accessManagementPort.revokeSessionsByScope(
                MerchantAccessProfile.SELLER_PLATFORM_CODE,
                MerchantAccessProfile.MERCHANT_SCOPE_KEY,
                event.merchantId()
        );
    }

    @EventListener
    public void onMerchantClosed(MerchantClosedEvent event) {
        log.warn("Merchant closed merchantId={}. Revoking active seller portal sessions.", event.merchantId());
        accessManagementPort.revokeSessionsByScope(
                MerchantAccessProfile.SELLER_PLATFORM_CODE,
                MerchantAccessProfile.MERCHANT_SCOPE_KEY,
                event.merchantId()
        );
    }
}
