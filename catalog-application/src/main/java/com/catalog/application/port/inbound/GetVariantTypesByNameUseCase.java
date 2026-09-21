package com.catalog.application.port.inbound;

import com.catalog.application.model.read.GetVariantTypesByNameQuery;
import com.catalog.application.model.read.VariantTypeResult;

public interface GetVariantTypesByNameUseCase {
    VariantTypeResult execute(GetVariantTypesByNameQuery query);
}
