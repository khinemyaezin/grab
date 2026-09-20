package com.catalog.application.port.inbound;

import com.catalog.application.query.GetVariantQuery;
import com.catalog.application.query.GetVariantResult;

public interface GetVariantUseCase {
    GetVariantResult execute(GetVariantQuery query);
}
