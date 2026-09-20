package com.catalog.application.port.inbound;

import com.catalog.application.query.GetVariantOptionsByNameQuery;
import com.catalog.application.query.VariantOptionResult;

public interface GetVariantOptionsByNameUseCase {
    VariantOptionResult execute(GetVariantOptionsByNameQuery query);
}
