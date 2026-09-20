package com.catalog.application.port.inbound;

import com.catalog.application.query.GetVariantTypesByNameQuery;
import com.catalog.application.query.VariantTypeResult;

public interface GetVariantTypesByNameUseCase {
    VariantTypeResult execute(GetVariantTypesByNameQuery query);
}
