package com.merchant.application.port.inbound;

import com.merchant.application.model.write.AutoApproveMerchantApplicationCommand;

public interface AutoApproveMerchantApplicationUseCase {
    void execute(AutoApproveMerchantApplicationCommand command);
}
