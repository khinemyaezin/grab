package com.catalog.application.port.inbound;

import com.catalog.application.model.read.FindCatalogVariantSliceQuery;
import com.catalog.application.model.read.FindCatalogVariantSliceResult;

import java.util.Optional;

public interface FindCatalogVariantSliceUseCase {
    Optional<FindCatalogVariantSliceResult> execute(FindCatalogVariantSliceQuery query);
}
