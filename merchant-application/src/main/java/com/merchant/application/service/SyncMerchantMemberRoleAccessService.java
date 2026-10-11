package com.merchant.application.service;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.merchant.application.model.write.SyncMerchantMemberRoleAccessCommand;
import com.merchant.application.port.inbound.SyncMerchantMemberRoleAccessUseCase;
import com.merchant.application.port.outbound.IdentityAccessManagementPort;
import com.merchant.application.port.outbound.IdentityAccessManagementPort.ReplaceAccessRequest;
import com.merchant.application.security.MerchantAdminAccessProfile;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@RequiredArgsConstructor
public class SyncMerchantMemberRoleAccessService implements SyncMerchantMemberRoleAccessUseCase {
    private static final Logger log = Loggers.getLogger(SyncMerchantMemberRoleAccessService.class);

    private final IdentityAccessManagementPort identityAccessManagementPort;

    @Override
    public void execute(SyncMerchantMemberRoleAccessCommand command) {
        String userId = command.userId().getValue();
        String merchantId = command.merchantId().getValue();
        String previousRole = command.previousRole();
        String newRole = command.newRole();
        String previousRoleCode = MerchantAdminAccessProfile.toRoleCode(previousRole);
        String newRoleCode = MerchantAdminAccessProfile.toRoleCode(newRole);
        Set<String> authorities = command.authorities();
        log.info(
                "Syncing member role access userId={} in merchantId={} from {} to {}",
                userId,
                merchantId,
                previousRoleCode,
                newRoleCode
        );
        ReplaceAccessRequest request = new ReplaceAccessRequest(
                userId,
                previousRoleCode,
                newRoleCode,
                MerchantAdminAccessProfile.MERCHANT_SCOPE_KEY,
                merchantId,
                authorities
        );
        identityAccessManagementPort.replaceAccess(request);
    }
}
