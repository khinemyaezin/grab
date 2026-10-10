package com.merchant.application.service;

import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.framework.security.role.RoleDeclarationPublicationPort;
import com.merchant.application.model.write.PublishMerchantRoleDeclarationCommand;
import com.merchant.application.port.inbound.PublishMerchantRoleDeclarationUseCase;
import com.merchant.application.security.MerchantAdminAccessProfile;

public class PublishMerchantRoleDeclarationService implements PublishMerchantRoleDeclarationUseCase {
    private static final Logger log = Loggers.getLogger(PublishMerchantRoleDeclarationService.class);
    private final RoleDeclarationPublicationPort publication;

    public PublishMerchantRoleDeclarationService(RoleDeclarationPublicationPort publication) {
        this.publication = publication;
    }

    @Override
    public void execute(PublishMerchantRoleDeclarationCommand command) {
        RoleDeclarationPublicationPort.PublicationResult result =
                publication.enqueue(MerchantAdminAccessProfile.DECLARATION);
        log.info("Role declaration publication roleCode={} outcome={}",
                MerchantAdminAccessProfile.ADMIN_ROLE_CODE, result);
    }
}
