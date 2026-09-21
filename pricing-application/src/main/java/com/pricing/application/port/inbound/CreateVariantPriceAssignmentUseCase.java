package com.pricing.application.port.inbound;

import com.pricing.application.model.write.CreateVariantPriceAssignmentCommand;
import com.pricing.application.model.write.CreateVariantPriceAssignmentResult;

public interface CreateVariantPriceAssignmentUseCase {
    CreateVariantPriceAssignmentResult execute(CreateVariantPriceAssignmentCommand command);
}
