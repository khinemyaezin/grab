package com.grab.store.merchant.internal.event;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.identity.port.AccessManagementPort;
import com.merchant.domain.enums.MemberStatus;
import com.merchant.domain.event.MerchantAccessProfile;
import com.merchant.domain.event.MerchantMemberCreatedEvent;
import com.merchant.domain.event.MerchantMemberRemovedEvent;
import com.merchant.domain.event.MerchantMemberRoleChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MerchantMemberAccessSyncListener {
    private static final Logger log = Loggers.getLogger(MerchantMemberAccessSyncListener.class);

    private final AccessManagementPort accessManagementPort;

    @EventListener
    public void onMemberCreated(MerchantMemberCreatedEvent event) {
        if (event.isAdmin() && MemberStatus.ACTIVE.name().equals(event.status())) {
            log.info(
                    "Syncing initial admin access for userId={} in merchantId={}",
                    event.userId(),
                    event.merchantId()
            );
            accessManagementPort.replaceAccess(new AccessManagementPort.ReplaceAccessRequest(
                    event.userId(),
                    MerchantAccessProfile.SELLER_PLATFORM_CODE,
                    null,
                    MerchantAccessProfile.ADMIN_ROLE_CODE,
                    MerchantAccessProfile.MERCHANT_SCOPE_KEY,
                    event.merchantId()
            ));
        }
    }

    @EventListener
    public void onMemberRoleChanged(MerchantMemberRoleChangedEvent event) {
        log.info(
                "Syncing role change for memberId={} userId={} in merchantId={} from {} to {}",
                event.memberId(),
                event.userId(),
                event.merchantId(),
                event.previousRole(),
                event.newRole()
        );
        accessManagementPort.replaceAccess(new AccessManagementPort.ReplaceAccessRequest(
                event.userId(),
                MerchantAccessProfile.SELLER_PLATFORM_CODE,
                MerchantAccessProfile.toRoleCode(event.previousRole()),
                MerchantAccessProfile.toRoleCode(event.newRole()),
                MerchantAccessProfile.MERCHANT_SCOPE_KEY,
                event.merchantId()
        ));
    }

    @EventListener
    public void onMemberRemoved(MerchantMemberRemovedEvent event) {
        log.info(
                "Revoking access for removed memberId={} userId={} in merchantId={}",
                event.memberId(),
                event.userId(),
                event.merchantId()
        );
        accessManagementPort.revokeAccess(new AccessManagementPort.RevokeAccessRequest(
                event.userId(),
                MerchantAccessProfile.SELLER_PLATFORM_CODE,
                MerchantAccessProfile.toRoleCode(event.role()),
                MerchantAccessProfile.MERCHANT_SCOPE_KEY,
                event.merchantId()
        ));
    }
}
