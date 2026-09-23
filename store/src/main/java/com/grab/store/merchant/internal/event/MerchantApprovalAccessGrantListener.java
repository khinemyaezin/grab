package com.grab.store.merchant.internal.event;

import com.grab.store.identity.port.AccessManagementPort;
import com.merchant.domain.event.MerchantApprovedEvent;
import com.merchant.domain.policy.MerchantApprovalAccessPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantApprovalAccessGrantListener {
    private final AccessManagementPort accessManagementPort;
    private final MerchantApprovalAccessPolicy accessPolicy;

    @EventListener
    public void onMerchantApproved(MerchantApprovedEvent event) {

        MerchantApprovalAccessPolicy.MerchantApprovalContext context =
                new MerchantApprovalAccessPolicy.MerchantApprovalContext(event.merchantId());

    }
}
