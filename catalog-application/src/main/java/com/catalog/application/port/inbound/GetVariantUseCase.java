package com.catalog.application.port.inbound;

import com.catalog.application.model.read.GetVariantQuery;
import com.catalog.application.model.read.GetVariantResult;

public interface GetVariantUseCase {
    GetVariantResult execute(GetVariantQuery query);
}
