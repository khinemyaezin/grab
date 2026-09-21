package com.merchant.application.port.inbound;

import com.merchant.application.model.write.StartMerchantApplicationCommand;
import com.merchant.application.model.write.MerchantAccountResult;

public interface StartMerchantApplicationUseCase {
    MerchantAccountResult execute(StartMerchantApplicationCommand command);
}
