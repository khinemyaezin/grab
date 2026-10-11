package com.merchant.application.service;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.merchant.application.model.write.RevokeMerchantMemberAccessCommand;
import com.merchant.application.port.inbound.RevokeMerchantMemberAccessUseCase;
import com.merchant.application.port.outbound.IdentityAccessManagementPort;
import com.merchant.application.port.outbound.IdentityAccessManagementPort.RevokeAccessRequest;
import com.merchant.application.security.MerchantAdminAccessProfile;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RevokeMerchantMemberAccessService implements RevokeMerchantMemberAccessUseCase {
    private static final Logger log = Loggers.getLogger(RevokeMerchantMemberAccessService.class);

    private final IdentityAccessManagementPort identityAccessManagementPort;

    @Override
    public void execute(RevokeMerchantMemberAccessCommand command) {
        String userId = command.userId().getValue();
        String merchantId = command.merchantId().getValue();
        String role = command.role();
        String roleCode = MerchantAdminAccessProfile.toRoleCode(role);
        log.info(
                "Revoking member access userId={} roleCode={} in merchantId={}",
                userId,
                roleCode,
                merchantId
        );
        RevokeAccessRequest request = new RevokeAccessRequest(
                userId,
                roleCode,
                MerchantAdminAccessProfile.MERCHANT_SCOPE_KEY,
                merchantId
        );
        identityAccessManagementPort.revokeAccess(request);
    }
}
