package com.merchant.application.port.inbound;

import com.merchant.application.model.write.PublishMerchantRoleDeclarationCommand;

public interface PublishMerchantRoleDeclarationUseCase {
    void execute(PublishMerchantRoleDeclarationCommand command);
}
