package com.merchant.application.port.inbound;

import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.model.write.ProvisionMerchantAdminCommand;

public interface ProvisionMerchantAdminUseCase {
    MerchantMemberResult execute(ProvisionMerchantAdminCommand command);
}
