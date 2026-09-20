package com.merchant.application.port.inbound;

import com.merchant.application.model.write.UpdateMerchantProfileCommand;
import com.merchant.application.model.write.MerchantAccountResult;

public interface UpdateMerchantProfileUseCase {
    MerchantAccountResult execute(UpdateMerchantProfileCommand command);
}
