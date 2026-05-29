package com.grab.store.catalog.internal.query.handler;

import com.catalog.infrastructure.repository.jpa.VariantTypeQueryRepository;
import com.catalog.infrastructure.view.VariantTypeView;
import com.grab.framework.cqrs.query.QueryHandler;
import com.grab.framework.logger.Logger;
import com.grab.framework.logger.Loggers;
import com.grab.store.catalog.internal.query.GetVariantTypesByNameQuery;
import com.grab.store.catalog.internal.query.VariantTypeResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GetVariantTypesByNameQueryHandler implements QueryHandler<GetVariantTypesByNameQuery, VariantTypeResult> {

    private static final Logger log = Loggers.getLogger(GetVariantTypesByNameQueryHandler.class);

    private final VariantTypeQueryRepository variantTypeQueryRepository;

    @Override
    public VariantTypeResult handle(GetVariantTypesByNameQuery query) {
        log.debug("Handling GetVariantTypesByNameQuery for name={}", query.name());

        List<VariantTypeView> views = variantTypeQueryRepository.findByName(query.name());

        List<VariantTypeResult.VariantTypeItem> items = views.stream()
                .map(this::toResultItem)
                .toList();

        return new VariantTypeResult(items);
    }

    private VariantTypeResult.VariantTypeItem toResultItem(VariantTypeView view) {

        return new VariantTypeResult.VariantTypeItem(
                view.id(),
                view.name()
        );
    }

    @Override
    public Class<GetVariantTypesByNameQuery> getQueryType() {
        return GetVariantTypesByNameQuery.class;
    }
}
