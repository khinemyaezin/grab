package com.merchant.application.port.inbound;

import com.merchant.application.model.write.ChangeStorefrontLifecycleCommand;
import com.merchant.application.model.write.StorefrontResult;

public interface ChangeStorefrontLifecycleUseCase {
    StorefrontResult execute(ChangeStorefrontLifecycleCommand command);
}
