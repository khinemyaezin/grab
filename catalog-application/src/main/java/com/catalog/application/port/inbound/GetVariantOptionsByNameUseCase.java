package com.catalog.application.port.inbound;

import com.catalog.application.model.read.GetVariantOptionsByNameQuery;
import com.catalog.application.model.read.VariantOptionResult;

public interface GetVariantOptionsByNameUseCase {
    VariantOptionResult execute(GetVariantOptionsByNameQuery query);
}
