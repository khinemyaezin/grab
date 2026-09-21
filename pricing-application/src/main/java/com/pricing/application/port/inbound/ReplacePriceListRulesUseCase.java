package com.pricing.application.port.inbound;

import com.pricing.application.model.write.ReplacePriceListRulesCommand;
import com.pricing.application.model.write.PriceListResult;

public interface ReplacePriceListRulesUseCase {
    PriceListResult execute(ReplacePriceListRulesCommand command);
}
