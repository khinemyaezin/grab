package com.merchant.application.port.inbound;

import com.grab.framework.security.role.RoleDeclarationPublicationPort.PublicationResult;
import com.merchant.application.model.write.PublishMerchantRoleDeclarationCommand;

public interface PublishMerchantRoleDeclarationUseCase {
    PublicationResult execute(PublishMerchantRoleDeclarationCommand command);
}
