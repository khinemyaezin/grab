package com.pricing.application.port.inbound;

import com.pricing.application.model.read.ListPricePreferencesQuery;
import java.util.List;
import com.pricing.application.model.write.PricePreferenceResult;

public interface ListPricePreferencesUseCase {
    List<PricePreferenceResult> execute(ListPricePreferencesQuery query);
}
