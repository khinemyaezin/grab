package com.merchant.application.port.inbound;

import com.merchant.application.model.write.InviteMerchantMemberCommand;
import com.merchant.application.model.write.MerchantMemberResult;

public interface InviteMerchantMemberUseCase {
    MerchantMemberResult execute(InviteMerchantMemberCommand command);
}
