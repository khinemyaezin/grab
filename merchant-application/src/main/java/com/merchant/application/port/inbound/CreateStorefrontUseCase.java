package com.merchant.application.port.inbound;

import com.merchant.application.model.write.CreateStorefrontCommand;
import com.merchant.application.model.write.StorefrontResult;

public interface CreateStorefrontUseCase {
    StorefrontResult execute(CreateStorefrontCommand command);
}
