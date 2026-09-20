package com.pricing.application.port.inbound;

import com.pricing.application.model.read.ListVariantPriceSetLinksQuery;
import java.util.List;
import com.pricing.application.model.read.VariantPriceSetLinkResult;

public interface ListVariantPriceSetLinksUseCase {
    List<VariantPriceSetLinkResult> execute(ListVariantPriceSetLinksQuery query);
}
