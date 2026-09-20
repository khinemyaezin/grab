package com.merchant.application.port.inbound;

import com.merchant.application.model.write.SubmitMerchantApplicationCommand;
import com.merchant.application.model.write.MerchantAccountResult;

public interface SubmitMerchantApplicationUseCase {
    MerchantAccountResult execute(SubmitMerchantApplicationCommand command);
}
