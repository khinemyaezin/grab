package com.pricing.application.port.inbound;

import com.pricing.application.model.read.ListPriceListsQuery;
import java.util.List;
import com.pricing.application.model.write.PriceListResult;

public interface ListPriceListsUseCase {
    List<PriceListResult> execute(ListPriceListsQuery query);
}
