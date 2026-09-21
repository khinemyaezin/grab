package com.catalog.application.port.inbound;

import com.catalog.application.model.read.FindPublishedVariantQuery;
import com.catalog.application.model.read.FindPublishedVariantResult;

import java.util.Optional;

public interface FindPublishedVariantUseCase {
    Optional<FindPublishedVariantResult> execute(FindPublishedVariantQuery query);
}
