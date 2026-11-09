package com.grab.app.merchandising.catalog.internal.query.handler;

import com.catalog.application.model.read.VariationMatrixQuery;
import com.catalog.application.model.read.VariationMatrixResult;
import com.catalog.application.port.inbound.VariationMatrixUseCase;
import com.grab.app.merchandising.catalog.internal.config.CatalogReadTransactional;
import com.grab.framework.cqrs.query.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class VariationMatrixQueryHandler implements QueryHandler<VariationMatrixQuery, VariationMatrixResult> {

    private final VariationMatrixUseCase variationMatrixUseCase;

    public VariationMatrixQueryHandler(VariationMatrixUseCase variationMatrixUseCase) {
        this.variationMatrixUseCase = variationMatrixUseCase;
    }

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
