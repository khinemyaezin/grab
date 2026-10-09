package com.merchant.application.port.inbound;

import com.merchant.application.model.write.MerchantMemberResult;
import com.merchant.application.model.write.RecordMemberProvisioningResultCommand;

public interface RecordMemberProvisioningResultUseCase {
    MerchantMemberResult execute(RecordMemberProvisioningResultCommand command);
}
