package com.merchant.application.port.inbound;

import com.merchant.application.model.write.RevokeMerchantMemberAccessCommand;

public interface RevokeMerchantMemberAccessUseCase {
    void execute(RevokeMerchantMemberAccessCommand command);
}
