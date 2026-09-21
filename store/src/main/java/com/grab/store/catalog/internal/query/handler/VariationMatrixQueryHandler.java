package com.grab.store.catalog.internal.query.handler;

import com.catalog.application.port.inbound.VariationMatrixUseCase;
import com.catalog.application.model.read.VariationMatrixQuery;
import com.catalog.application.model.read.VariationMatrixResult;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.store.catalog.internal.config.CatalogReadTransactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VariationMatrixQueryHandler implements QueryHandler<VariationMatrixQuery, VariationMatrixResult> {

    private final VariationMatrixUseCase variationMatrixUseCase;

    @Override
    @CatalogReadTransactional
    public VariationMatrixResult handle(VariationMatrixQuery query) {
        return variationMatrixUseCase.execute(query);
    }

    @Override
    public Class<VariationMatrixQuery> getQueryType() {
        return VariationMatrixQuery.class;
    }
}
