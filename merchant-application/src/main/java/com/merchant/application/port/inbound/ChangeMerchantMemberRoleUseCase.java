package com.merchant.application.port.inbound;

import com.merchant.application.model.write.ChangeMerchantMemberRoleCommand;
import com.merchant.application.model.write.MerchantMemberResult;

public interface ChangeMerchantMemberRoleUseCase {
    MerchantMemberResult execute(ChangeMerchantMemberRoleCommand command);
}
