package com.catalog.application.port.inbound;

import com.catalog.application.query.VariationMatrixQuery;
import com.catalog.application.query.VariationMatrixResult;

public interface VariationMatrixUseCase {
    VariationMatrixResult execute(VariationMatrixQuery query);
}
