package com.catalog.application.port.inbound;

import com.catalog.application.model.read.VariationMatrixQuery;
import com.catalog.application.model.read.VariationMatrixResult;

public interface VariationMatrixUseCase {
    VariationMatrixResult execute(VariationMatrixQuery query);
}
