package com.merchant.application.port.inbound;

import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.model.write.RemoveMerchantMemberCommand;

public interface RemoveMerchantMemberUseCase {
    MerchantMemberResult execute(RemoveMerchantMemberCommand command);
}
