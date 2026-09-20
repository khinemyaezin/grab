package com.merchant.application.port.inbound;

import com.merchant.application.model.write.ChangeMerchantLifecycleCommand;
import com.merchant.application.model.write.MerchantAccountResult;

public interface ChangeMerchantLifecycleUseCase {
    MerchantAccountResult execute(ChangeMerchantLifecycleCommand command);
}
