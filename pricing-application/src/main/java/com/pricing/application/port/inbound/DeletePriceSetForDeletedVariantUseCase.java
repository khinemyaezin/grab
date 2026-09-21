package com.pricing.application.port.inbound;

import com.pricing.application.model.write.DeletePriceSetForDeletedVariantCommand;
import com.pricing.application.model.write.DeletePriceSetForDeletedVariantResult;

public interface DeletePriceSetForDeletedVariantUseCase {
    DeletePriceSetForDeletedVariantResult execute(DeletePriceSetForDeletedVariantCommand command);
}
