package com.merchant.application.port.inbound;

import com.merchant.application.model.write.UpdateStorefrontProfileCommand;
import com.merchant.application.model.write.StorefrontResult;

public interface UpdateStorefrontProfileUseCase {
    StorefrontResult execute(UpdateStorefrontProfileCommand command);
}
