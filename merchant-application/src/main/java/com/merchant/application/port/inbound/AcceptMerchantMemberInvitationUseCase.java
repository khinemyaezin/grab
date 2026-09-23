package com.merchant.application.port.inbound;

import com.merchant.application.model.write.AcceptMerchantMemberInvitationCommand;
import com.merchant.application.model.write.MerchantMemberResult;

public interface AcceptMerchantMemberInvitationUseCase {
    MerchantMemberResult execute(AcceptMerchantMemberInvitationCommand command);
}
